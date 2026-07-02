package com.windrr.boat.data.remote.model

data class ReceiptListResponse(
    val success: Boolean,
    val status: Int,
    val data: ReceiptListData,
)

data class ReceiptListData(
    val receipts: List<ReceiptItem>,
    val totalCount: Int,
    val pagination: ReceiptPagination,
)

data class ReceiptItem(
    val receiptId: String,
    val itemName: String,
    val brandName: String?,
    val paymentLocation: String?,
    val paymentDate: String?,
    val totalAmount: Int?,
    val periodMonths: Int?,
    val expiresOn: String?,
    val category: String?,
    val subCategory: String?,
    val memo: String?,
    val requiresPhysicalReceipt: Boolean,
    val receiptFileIds: List<String>,
    val imageUrl: String?,
    val warrantyDDay: Int?,
    val serialNumber: String?,
    val supportUrl: String?,
    val registeredAt: String?,
)

data class ReceiptPagination(
    val hasNext: Boolean,
    val limit: Int,
    val nextCursor: String?,
    val totalCount: Int,
)
