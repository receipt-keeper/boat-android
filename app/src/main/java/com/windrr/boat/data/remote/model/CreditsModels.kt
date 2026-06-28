package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

/** GET /api/v1/credits 응답 */
data class CreditsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("status")  val status: Int,
    @SerializedName("data")    val data: CreditsData,
)

data class CreditsData(
    @SerializedName("remainingCount")    val remainingCount: Int = 0,
    @SerializedName("totalGrantedCount") val totalGrantedCount: Int = 0,
    @SerializedName("usedCount")         val usedCount: Int = 0,
)
