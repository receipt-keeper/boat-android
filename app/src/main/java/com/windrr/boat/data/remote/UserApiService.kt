package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.UserResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET

interface UserApiService {
    /**
     * 현재 로그인 사용자 프로필 조회. AccessToken(Bearer)은 TokenInterceptor가 자동 주입.
     * 알림·마케팅 동의 설정은 GET /api/v1/notifications/settings 에서 별도 조회.
     */
    @GET("api/v1/users/me")
    suspend fun getMe(): UserResponse

    /**
     * 회원 탈퇴. 로그인 정보와 사용자 계정 데이터를 삭제한다.
     * AccessToken(Bearer)은 TokenInterceptor가 자동 주입한다. 성공 시 204(빈 본문).
     */
    @DELETE("api/v1/users/me")
    suspend fun deleteAccount(): Response<Unit>
}
