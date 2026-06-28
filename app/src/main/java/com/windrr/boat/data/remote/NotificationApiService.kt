package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.NotificationSettingsResponse
import com.windrr.boat.data.remote.model.UpdateNotificationSettingsRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface NotificationApiService {
    @GET("api/v1/notifications/settings")
    suspend fun getNotificationSettings(): NotificationSettingsResponse

    @PATCH("api/v1/notifications/settings")
    suspend fun updateNotificationSettings(
        @Body request: UpdateNotificationSettingsRequest,
    ): NotificationSettingsResponse
}
