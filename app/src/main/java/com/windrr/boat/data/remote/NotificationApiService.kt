package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.NotificationDetailResponse
import com.windrr.boat.data.remote.model.NotificationListResponse
import com.windrr.boat.data.remote.model.NotificationSettingsResponse
import com.windrr.boat.data.remote.model.RegisterDeviceRequest
import com.windrr.boat.data.remote.model.UpdateNotificationSettingsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificationApiService {

    /** 알림 목록 조회 */
    @GET("api/v1/notifications")
    suspend fun getNotifications(): NotificationListResponse

    /** 알림 읽음 처리 — 응답 data.readAt에 읽음 시각이 채워진다. */
    @PATCH("api/v1/notifications/{notificationId}")
    suspend fun markNotificationRead(
        @Path("notificationId") notificationId: String,
    ): NotificationDetailResponse

    @GET("api/v1/notifications/settings")
    suspend fun getNotificationSettings(): NotificationSettingsResponse

    @PATCH("api/v1/notifications/settings")
    suspend fun updateNotificationSettings(
        @Body request: UpdateNotificationSettingsRequest,
    ): NotificationSettingsResponse

    /**
     * FCM 디바이스 등록 — 로그인 사용자의 디바이스를 FCM registration token으로 멱등 upsert.
     * 성공 시 204(본문 없음).
     */
    @PUT("api/v1/notifications/devices")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): Response<Unit>

    /**
     * FCM 디바이스 해제 — FCM registration token 기준. 로그아웃 전에 호출. 미등록 token이어도 멱등하게 204.
     */
    @DELETE("api/v1/notifications/devices/{token}")
    suspend fun unregisterDevice(@Path("token") token: String): Response<Unit>
}
