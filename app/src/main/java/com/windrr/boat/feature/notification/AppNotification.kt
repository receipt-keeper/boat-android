package com.windrr.boat.feature.notification

/**
 * 알림 목록 아이템 모델.
 * 사용자가 확인(클릭)하기 전까지 목록에 저장해 보여준다.
 *
 * @property id           알림 식별자
 * @property productName  관련 제품명
 * @property message      알림 메시지
 * @property date         표시용 날짜 (예: "2026.06.15")
 * @property thumbnailUrl 제품 썸네일 URL (없으면 placeholder)
 */
data class AppNotification(
    val id: Long,
    val productName: String,
    val message: String,
    val date: String,
    val thumbnailUrl: String? = null,
)
