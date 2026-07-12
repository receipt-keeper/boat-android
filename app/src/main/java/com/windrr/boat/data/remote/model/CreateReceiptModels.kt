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
    @SerializedName("expires_on")                val expiresOn: String? = null,
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

/**
 * 영수증 수정 요청 바디 — PATCH /api/v1/receipts/{receipt_id}.
 * paymentLocation은 수정 UI에 입력 필드가 없어 제외한다.
 * receiptFileIds는 수정 후 최종 남길 전체 목록(기존 유지분 + 새로 업로드해 받은 fileId)을 보낸다.
 */
data class UpdateReceiptRequest(
    @SerializedName("item_name")                 val itemName: String,
    @SerializedName("brand_name")                val brandName: String? = null,
    @SerializedName("serial_number")              val serialNumber: String? = null,
    @SerializedName("payment_date")              val paymentDate: String? = null,
    @SerializedName("total_amount")              val totalAmount: Int? = null,
    @SerializedName("period_months")             val periodMonths: Int? = null,
    @SerializedName("expires_on")                val expiresOn: String? = null,
    @SerializedName("category")                  val category: String? = null,
    @SerializedName("sub_category")              val subCategory: String? = null,
    @SerializedName("memo")                      val memo: String? = null,
    @SerializedName("requires_physical_receipt") val requiresPhysicalReceipt: Boolean = false,
    @SerializedName("receipt_file_ids")          val receiptFileIds: List<String> = emptyList(),
)

/** 영수증 수정 응답 — data는 목록 아이템과 동일 형태(camelCase). */
data class UpdateReceiptResponse(
    val success: Boolean,
    val status: Int,
    val data: ReceiptItem,
)
