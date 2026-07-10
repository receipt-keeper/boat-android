package com.windrr.boat.feature.home

import com.windrr.boat.data.remote.model.ReceiptItem
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * AS 만료 예정 기기 (홈 가로형 카드).
 */
data class ExpiringWarranty(
    val receiptId: String,
    val productName: String,
    val brand: String,
    val purchaseDate: String,
    /** "MM월 dd일(요일)" — "%1$s 보증종료"(home_warranty_end_label)와 함께 쓴다. */
    val expiryLabel: String,
    val dDay: Int,
    val category: String? = null,
    val subCategory: String? = null,
)

/**
 * 최근 등록된 영수증 (홈 세로형 리스트).
 */
data class RecentReceipt(
    val receiptId: String,
    val productName: String,
    val purchaseDate: String,
    val daysAgo: Int,
    val category: String? = null,
    val subCategory: String? = null,
)

fun ReceiptItem.toExpiringWarranty() = ExpiringWarranty(
    receiptId = receiptId,
    productName = itemName,
    brand = brandName?.takeIf { it.isNotBlank() } ?: "-",
    purchaseDate = paymentDate.toDotDate(),
    expiryLabel = expiresOn.toExpiryLabel(),
    dDay = warrantyDDay ?: 0,
    category = category,
    subCategory = subCategory,
)

fun ReceiptItem.toRecentReceipt() = RecentReceipt(
    receiptId = receiptId,
    productName = itemName,
    purchaseDate = paymentDate.toDotDate(),
    daysAgo = registeredAt.daysAgo(),
    category = category,
    subCategory = subCategory,
)

/** "2026-06-29" → "2026.06.29" */
private fun String?.toDotDate(): String {
    if (this.isNullOrBlank()) return "-"
    val parts = split("-")
    return if (parts.size == 3) "${parts[0]}.${parts[1]}.${parts[2]}" else this
}

/** "2026-06-29" → "06월 29일(월)" */
private fun String?.toExpiryLabel(): String {
    if (this.isNullOrBlank()) return "-"
    return runCatching {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).parse(this) ?: return "-"
        SimpleDateFormat("MM월 dd일(E)", Locale.KOREA).format(date)
    }.getOrDefault("-")
}

/** "2026-06-29T12:00:00" → 오늘까지 경과 일수 */
private fun String?.daysAgo(): Int {
    if (this.isNullOrBlank()) return 0
    return runCatching {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
        val registered = sdf.parse(this) ?: return 0
        val diffMs = System.currentTimeMillis() - registered.time
        (diffMs / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(0)
    }.getOrDefault(0)
}
