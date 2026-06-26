package com.windrr.boat.feature.home

/**
 * AS 만료 예정 기기 (홈 가로형 카드).
 */
data class ExpiringWarranty(
    val id: Long,
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
    val id: Long,
    val productName: String,
    val purchaseDate: String,
    val daysAgo: Int,
    val thumbnailUrl: String? = null,
)

/** 임시 데이터 — AS 만료 예정 (만료 임박 오름차순) */
fun sampleExpiringWarranties(): List<ExpiringWarranty> = listOf(
    ExpiringWarranty(1, "MacBook Pro 16", "Apple", "2025.03.13", "~2033.04.40", dDay = 20),
    ExpiringWarranty(2, "LG 그램 17", "LG전자", "2024.11.02", "~2026.11.01", dDay = 25),
    ExpiringWarranty(3, "삼성 비스포크 냉장고", "삼성전자", "2023.07.21", "~2025.07.20", dDay = 28),
)

/** 임시 데이터 — 최근 등록된 영수증 (최근순, 최대 5) */
fun sampleRecentReceipts(): List<RecentReceipt> = listOf(
    RecentReceipt(1, "IPad Pro 13", "2027. 12. 34", daysAgo = 2),
    RecentReceipt(2, "IPad Pro 13", "2027. 12. 34", daysAgo = 2),
    RecentReceipt(3, "IPad Pro 13", "2027. 12. 34", daysAgo = 2),
    RecentReceipt(4, "IPad Pro 13", "2027. 12. 34", daysAgo = 2),
    RecentReceipt(5, "IPad Pro 13", "2027. 12. 34", daysAgo = 2),
)
