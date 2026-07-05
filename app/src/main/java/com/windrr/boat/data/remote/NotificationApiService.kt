package com.windrr.boat.data.remote

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
    @GET("api/v1/notifications/settings")
    suspend fun getNotificationSettings(): NotificationSettingsResponse

    @PATCH("api/v1/notifications/settings")
    suspend fun updateNotificationSettings(
        @Body request: UpdateNotificationSettingsRequest,
    ): NotificationSettingsResponse

    /**
     * FCM 디바이스 등록 — 로그인 사용자의 디바이스를 FID로 멱등 upsert. 성공 시 204(본문 없음).
     */
    @PUT("api/v1/notifications/devices")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): Response<Unit>

    /**
     * FCM 디바이스 해제 — FID 기준. 로그아웃 전에 호출. 미등록 fid여도 멱등하게 204.
     */
    @DELETE("api/v1/notifications/devices/{fid}")
    suspend fun unregisterDevice(@Path("fid") fid: String): Response<Unit>
}
