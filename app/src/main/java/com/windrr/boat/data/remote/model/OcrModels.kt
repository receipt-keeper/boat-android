package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class OcrResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("status")  val status: Int,
    @SerializedName("data")    val data: OcrData,
)

/**
 * 영수증 OCR 분석 결과. (저장하지 않고 분석 결과만 반환)
 * 결과 화면으로 Intent 전달하기 위해 Serializable.
 */
data class OcrData(
    @SerializedName("item_name")        val itemName: String? = null,
    @SerializedName("brand_name")       val brandName: String? = null,
    @SerializedName("payment_location") val paymentLocation: String? = null,
    @SerializedName("payment_date")     val paymentDate: String? = null,
    @SerializedName("total_amount")     val totalAmount: Long? = null,
    @SerializedName("period_months")    val periodMonths: Int? = null,
    @SerializedName("expires_on")       val expiresOn: String? = null,
    @SerializedName("category")         val category: String? = null,
    @SerializedName("sub_category")     val subCategory: String? = null,
    @SerializedName("needs_review")     val needsReview: Boolean = false,
    @SerializedName("warnings")         val warnings: List<String> = emptyList(),
) : Serializable
