package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.LoginRequest
import com.windrr.boat.data.remote.model.LoginResponse
import com.windrr.boat.data.remote.model.RefreshRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /**
     * AccessToken 재발급. refreshToken을 1회용으로 회전해 새 토큰 쌍을 발급한다.
     * 이미 회전된(사용된) refreshToken 재사용 시 서버가 401로 거부 → 사실상 로그아웃.
     *
     * 응답 구조는 login과 동일하므로 [LoginResponse]를 재사용한다.
     */
    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): LoginResponse
}
