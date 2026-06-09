package com.windrr.boat.data.remote.interceptor

import com.windrr.boat.data.local.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * 401 응답 시 RefreshToken으로 AccessToken을 재발급받고 원래 요청을 재시도
 *
 * TODO: 백엔드 토큰 갱신 API 확정 후 refreshApi 연동 구현
 *       현재는 갱신 실패 시 토큰 삭제(로그아웃) 처리만 포함
 */
class TokenAuthenticator(
    private val tokenDataStore: TokenDataStore
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 동일 요청에서 이미 재시도했으면 무한루프 방지를 위해 중단
        if (response.request.header("Authorization") == null) return null
        if (responseCount(response) >= 2) return null

        val refreshToken = runBlocking {
            tokenDataStore.refreshToken.first()
        } ?: run {
            // RefreshToken 없으면 로그아웃 처리
            runBlocking { tokenDataStore.clearTokens() }
            return null
        }

        // TODO: 서버 RefreshToken API 호출 후 새 토큰 저장
        // val newTokens = runBlocking { authApiService.refreshToken(refreshToken) }
        // runBlocking { tokenDataStore.saveTokens(newTokens.accessToken, newTokens.refreshToken) }
        // return response.request.newBuilder()
        //     .header("Authorization", "Bearer ${newTokens.accessToken}")
        //     .build()

        // 임시: 갱신 API 미연동 상태에서는 토큰 삭제 후 null 반환
        runBlocking { tokenDataStore.clearTokens() }
        return null
    }

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
