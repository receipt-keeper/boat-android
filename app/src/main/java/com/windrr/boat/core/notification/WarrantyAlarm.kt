package com.windrr.boat.core.notification

/**
 * AS 알람 예약 단위.
 *
 * @param receiptId     영수증 고유 식별자
 * @param productName   표시할 제품명
 * @param expiryDateMillis AS 만료 일시 (epoch millis, UTC)
 * @param daysBeforeExpiry 만료 몇 일 전에 알릴지 (0 = 당일)
 */
data class WarrantyAlarm(
    val receiptId: Long,
    val productName: String,
    val expiryDateMillis: Long,
    val daysBeforeExpiry: Int
)
