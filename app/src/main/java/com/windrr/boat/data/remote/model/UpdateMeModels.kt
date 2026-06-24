package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * PATCH /api/v1/users/me 요청 — 부분 수정.
 * null인 필드는 Gson 기본 설정상 직렬화에서 제외되어 "미전달 → 기존 값 유지"가 된다.
 */
data class UpdateMeRequest(
    @SerializedName("marketingConsent")    val marketingConsent: Boolean? = null,
    @SerializedName("notificationEnabled") val notificationEnabled: Boolean? = null,
)

/** PATCH /api/v1/users/me 응답 (변경 결과) */
data class UpdateMeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("status")  val status: Int,
    @SerializedName("data")    val data: UpdateMeData,
)

data class UpdateMeData(
    @SerializedName("marketingConsent")    val marketingConsent: Boolean? = null,
    @SerializedName("notificationEnabled") val notificationEnabled: Boolean? = null,
)
