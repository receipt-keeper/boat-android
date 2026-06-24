package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.UserResponse
import retrofit2.http.GET

interface UserApiService {
    /**
     * 현재 로그인 사용자 정보 조회. AccessToken(Bearer)은 TokenInterceptor가 자동 주입.
     * 이메일/이름/닉네임/프로필이미지/알림·마케팅 동의/무료 분석 토큰 잔량을 반환.
     */
    @GET("api/v1/users/me")
    suspend fun getMe(): UserResponse
}
