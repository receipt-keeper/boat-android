package com.windrr.boat.feature.notification

/**
 * 알림(목록 카드 탭 / 실제 푸시 탭) 공통 라우팅 규칙.
 * 두 진입점(NotificationListScreen, 푸시 탭 트램폴린)이 동일한 기준으로 이동 대상을 결정하도록 단일화한다.
 */
sealed class NotificationRoute {
    data class ReceiptDetail(val receiptId: String) : NotificationRoute()
    data object ReceiptRegister : NotificationRoute()
    data object Home : NotificationRoute()
    /** 특정 리소스를 가리키지 않는 알림 — 별도 이동 없음(목록은 머무름, 푸시는 홈으로만 이동). */
    data object None : NotificationRoute()
}

fun resolveNotificationRoute(
    resourceType: String?,
    resourceId: String?,
    kind: String?,
    messageType: String?,
): NotificationRoute = when {
    resourceType == "receipt" && !resourceId.isNullOrBlank() -> NotificationRoute.ReceiptDetail(resourceId)
    kind == "registration_prompt" -> NotificationRoute.ReceiptRegister
    kind == "receipt_registration_reminder" ||
        kind == "receipt_inactivity_reminder" ||
        kind == "receipt_analysis_reminder" ||
        messageType == "marketing" -> NotificationRoute.Home
    else -> NotificationRoute.None
}
