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
    /** 첨부 이미지 실 조회용 — contentPath에 인증 헤더를 붙여 GET하면 실제 이미지를 받는다. */
    val receiptFiles: List<ReceiptFile> = emptyList(),
    val imageUrl: String?,
    val warrantyDDay: Int?,
    val serialNumber: String?,
    val supportUrl: String?,
    val registeredAt: String?,
)

/** 첨부 파일 — [contentPath]는 BASE_URL을 붙이고 Authorization 헤더로 인증해 GET하면 실제 파일을 받는다. */
data class ReceiptFile(
    val fileId: String,
    val contentPath: String,
)

data class ReceiptPagination(
    val hasNext: Boolean,
    val limit: Int,
    val nextCursor: String?,
    val totalCount: Int,
)

/** 영수증 상세 조회 응답 — data는 목록 아이템과 동일 형태(camelCase). */
data class ReceiptDetailResponse(
    val success: Boolean,
    val status: Int,
    val data: ReceiptItem,
)
