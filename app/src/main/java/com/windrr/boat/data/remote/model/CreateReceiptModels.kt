package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * 영수증 등록 요청 바디 — POST /api/v1/receipts.
 * 서버는 요청 바디를 snake_case로 받으므로 @SerializedName으로 매핑한다.
 */
data class CreateReceiptRequest(
    @SerializedName("item_name")                 val itemName: String,
    @SerializedName("brand_name")                val brandName: String? = null,
    @SerializedName("payment_location")          val paymentLocation: String? = null,
    @SerializedName("payment_date")              val paymentDate: String? = null,
    @SerializedName("total_amount")              val totalAmount: Int? = null,
    @SerializedName("period_months")             val periodMonths: Int? = null,
    @SerializedName("category")                  val category: String? = null,
    @SerializedName("sub_category")              val subCategory: String? = null,
    @SerializedName("memo")                      val memo: String? = null,
    @SerializedName("requires_physical_receipt") val requiresPhysicalReceipt: Boolean = false,
    @SerializedName("receipt_file_ids")          val receiptFileIds: List<String> = emptyList(),
)

/** 영수증 등록 응답 — data는 목록 아이템과 동일 형태(camelCase). */
data class CreateReceiptResponse(
    val success: Boolean,
    val status: Int,
    val data: ReceiptItem,
)

/** 영수증 삭제 응답 — DELETE /api/v1/receipts/{receipt_id}. data 없이 성공 여부만 반환. */
data class DeleteReceiptResponse(
    val success: Boolean,
    val status: Int,
)
