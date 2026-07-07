package com.windrr.boat.feature.home

import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.model.ReceiptItem
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * AS 만료 예정 기기 (홈 가로형 카드).
 */
data class ExpiringWarranty(
    val receiptId: String,
    val productName: String,
    val vendor: String,
    val purchaseDate: String,
    val warrantyUntil: String,
    val dDay: Int,
    val thumbnailUrl: String? = null,
)

/**
 * 최근 등록된 영수증 (홈 세로형 리스트).
 */
data class RecentReceipt(
    val receiptId: String,
    val productName: String,
    val purchaseDate: String,
    val daysAgo: Int,
    val thumbnailUrl: String? = null,
)

fun ReceiptItem.toExpiringWarranty() = ExpiringWarranty(
    receiptId = receiptId,
    productName = itemName,
    vendor = paymentLocation?.takeIf { it.isNotBlank() }
        ?: brandName?.takeIf { it.isNotBlank() }
        ?: "-",
    purchaseDate = paymentDate.toDotDate(),
    warrantyUntil = expiresOn?.let { "~${it.toDotDate()}" } ?: "-",
    dDay = warrantyDDay ?: 0,
    thumbnailUrl = imageUrl.resolveImageUrl(),
)

fun ReceiptItem.toRecentReceipt() = RecentReceipt(
    receiptId = receiptId,
    productName = itemName,
    purchaseDate = paymentDate.toDotDate(),
    daysAgo = registeredAt.daysAgo(),
    thumbnailUrl = imageUrl.resolveImageUrl(),
)

/** "2026-06-29" → "2026.06.29" */
private fun String?.toDotDate(): String {
    if (this.isNullOrBlank()) return "-"
    val parts = split("-")
    return if (parts.size == 3) "${parts[0]}.${parts[1]}.${parts[2]}" else this
}

private fun String?.resolveImageUrl(): String? {
    if (this.isNullOrBlank()) return null
    return if (startsWith("http")) this else "${ApiClient.BASE_URL_PROD}${trimStart('/')}"
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
