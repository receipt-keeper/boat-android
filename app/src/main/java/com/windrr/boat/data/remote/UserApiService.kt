package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.UpdateMeRequest
import com.windrr.boat.data.remote.model.UpdateMeResponse
import com.windrr.boat.data.remote.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserApiService {
    /**
     * 현재 로그인 사용자 정보 조회. AccessToken(Bearer)은 TokenInterceptor가 자동 주입.
     * 이메일/이름/닉네임/프로필이미지/알림·마케팅 동의/무료 분석 토큰 잔량을 반환.
     */
    @GET("api/v1/users/me")
    suspend fun getMe(): UserResponse

    /**
     * 내 정보 부분 수정. 전달된 필드만 수정하며 미전달(null) 필드는 기존 값 유지.
     */
    @PATCH("api/v1/users/me")
    suspend fun updateMe(@Body request: UpdateMeRequest): UpdateMeResponse
}
