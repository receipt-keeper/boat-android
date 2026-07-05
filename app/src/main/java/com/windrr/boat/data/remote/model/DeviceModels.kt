package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * FCM 디바이스 등록 요청 — PUT /api/v1/notifications/devices.
 * FCM registration token 기준 멱등 upsert.
 */
data class RegisterDeviceRequest(
    @SerializedName("token")    val token: String,
    @SerializedName("platform") val platform: String = "android",
)
