package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.LoginRequest
import com.windrr.boat.data.remote.model.LoginResponse
import com.windrr.boat.data.remote.model.RefreshRequest
import com.windrr.boat.data.remote.model.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface AuthApiService {
    /** 가입된 사용자 로그인. 미가입 시 404 반환 → signup 호출 필요. */
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /** 신규 회원가입. Firebase idToken + 약관 동의 정보로 계정 생성 후 토큰 발급. */
    @POST("api/v1/auth/signup")
    suspend fun signup(@Body request: SignupRequest): LoginResponse

    /**
     * AccessToken 재발급. refreshToken을 1회용으로 회전해 새 토큰 쌍을 발급한다.
     * 이미 회전된(사용된) refreshToken 재사용 시 서버가 401로 거부 → 사실상 로그아웃.
     *
     * 응답 구조는 login과 동일하므로 [LoginResponse]를 재사용한다.
     */
    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): LoginResponse

    /**
     * 로그아웃. 제시된 refreshToken의 세션을 revoke하고 같은 세션의 accessToken도 즉시 무효화한다.
     * 성공 시 204 No Content(빈 본문)를 반환하므로 [Response]<Unit>으로 받아 isSuccessful만 확인한다.
     */
    @POST("api/v1/auth/logout")
    suspend fun logout(@Body request: RefreshRequest): Response<Unit>

    /**
     * 회원 탈퇴. 인증/사용자 데이터를 한 트랜잭션에서 삭제하고 성공 시 204, 실패 시 전체 롤백한다.
     * AccessToken(Bearer)은 TokenInterceptor가 자동 주입한다. 본문 없음.
     */
    @DELETE("api/v1/auth/me")
    suspend fun deleteAccount(): Response<Unit>
}
