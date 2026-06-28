package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.UserResponse
import retrofit2.http.GET

interface UserApiService {
    /**
     * 현재 로그인 사용자 프로필 조회. AccessToken(Bearer)은 TokenInterceptor가 자동 주입.
     * 알림·마케팅 동의 설정은 GET /api/v1/notifications/settings 에서 별도 조회.
     */
    @GET("api/v1/users/me")
    suspend fun getMe(): UserResponse
}
