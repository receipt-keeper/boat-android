package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

/** GET/PATCH /api/v1/notifications/settings 공통 응답 */
data class NotificationSettingsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("status")  val status: Int,
    @SerializedName("data")    val data: NotificationSettingsData,
)

data class NotificationSettingsData(
    @SerializedName("pushEnabled")      val pushEnabled: Boolean = false,
    @SerializedName("marketingConsent") val marketingConsent: Boolean = false,
)

/**
 * PATCH /api/v1/notifications/settings 요청.
 * null 필드는 직렬화에서 제외되어 기존 값이 유지된다.
 */
data class UpdateNotificationSettingsRequest(
    @SerializedName("pushEnabled")      val pushEnabled: Boolean? = null,
    @SerializedName("marketingConsent") val marketingConsent: Boolean? = null,
)
