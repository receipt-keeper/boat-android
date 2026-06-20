package com.windrr.boat.data.remote.interceptor

import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.local.TokenDataStore
import com.windrr.boat.data.remote.AuthApiService
import com.windrr.boat.data.remote.model.RefreshRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * 401 응답 시 RefreshToken으로 AccessToken을 재발급받고 원래 요청을 재시도한다.
 *
 * 동작 흐름:
 *   API 호출(만료 토큰) → 서버 401 → OkHttp가 authenticate() 자동 호출
 *     → refresh API로 새 토큰 쌍 발급 → DataStore 저장
 *     → 새 토큰을 단 Request 반환 → OkHttp가 원래 요청을 자동 재시도
 *   호출부(suspend fun)는 401·재발급·재시도 전 과정을 모르고 최종 성공 응답만 받는다.
 *
 * 안전장치:
 *   - [refreshApi]는 TokenInterceptor/Authenticator가 없는 별도 클라이언트라,
 *     만료 토큰 주입과 무한 재귀(refresh가 또 401 → authenticate 재진입)를 방지한다.
 *   - @Synchronized + 토큰 비교로 동시 401 시 중복 갱신(refresh token 중복 회전)을 막는다.
 *   - refresh도 실패(회전된 토큰 재사용 거부 등)하면 토큰을 삭제해 로그아웃 처리한다.
 */
class TokenAuthenticator(
    private val tokenDataStore: TokenDataStore,
    private val refreshApi: AuthApiService,
) : Authenticator {

    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        // Authorization 헤더가 없던 요청(로그인/리프레시 등)은 갱신 대상이 아님
        val failedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?: return null

        // 재발급 후 재시도한 요청까지 또 401이면 무한루프 방지를 위해 중단
        if (responseCount(response) >= 2) return null

        return runBlocking {
            // 다른 스레드가 이미 갱신을 끝냈다면 새 토큰으로 즉시 재시도 (중복 refresh 방지)
            val currentToken = tokenDataStore.accessToken.first()
            if (currentToken != null && currentToken != failedToken) {
                return@runBlocking response.newRequestWithToken(currentToken)
            }

            val refreshToken = tokenDataStore.refreshToken.first()
            if (refreshToken == null) {
                tokenDataStore.clearTokens()
                return@runBlocking null
            }

            try {
                val res = refreshApi.refresh(RefreshRequest(refreshToken))
                tokenDataStore.saveTokens(res.data.accessToken, res.data.refreshToken)
                BoatLog.i("AccessToken 재발급 성공")
                response.newRequestWithToken(res.data.accessToken)
            } catch (e: Exception) {
                // refresh도 401(회전된 토큰 재사용 등) 또는 네트워크 실패 → 로그아웃
                BoatLog.e("토큰 재발급 실패 — 로그아웃 처리", e)
                tokenDataStore.clearTokens()
                null
            }
        }
    }

    private fun Response.newRequestWithToken(token: String): Request =
        request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
