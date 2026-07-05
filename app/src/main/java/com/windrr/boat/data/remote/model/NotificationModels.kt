package com.windrr.boat.data.remote.model

/**
 * 알림 목록/상세 응답 모델.
 * GET  /api/v1/notifications            → [NotificationListResponse]
 * PATCH /api/v1/notifications/{id}(읽음) → [NotificationDetailResponse]
 */
data class NotificationListResponse(
    val success: Boolean,
    val status: Int,
    val data: NotificationListData,
)

data class NotificationListData(
    val notifications: List<NotificationDto> = emptyList(),
)

data class NotificationDetailResponse(
    val success: Boolean,
    val status: Int,
    val data: NotificationDto,
)

/**
 * 서버 알림 객체 (camelCase).
 * - [resourceType]/[resourceId]: 함께 있거나 함께 null. 화면 라우팅에 사용.
 * - [metadata]: 알림을 만든 쪽이 채우는 문자열 key-value. 카드 표시용(productName/subCategory/expiresAt 등).
 */
data class NotificationDto(
    val notificationId: String,
    val messageType: String? = null,
    val kind: String? = null,
    val title: String? = null,
    val message: String? = null,
    val resourceType: String? = null,
    val resourceId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: String? = null,
    val readAt: String? = null,
)
