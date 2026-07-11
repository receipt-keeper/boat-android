package com.windrr.boat.feature.notification

import com.windrr.boat.data.remote.model.NotificationDto
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 알림 목록 화면용 표시 모델.
 *
 * 서버 [NotificationDto] → 이 모델로 매핑한다. 카드 표시(productName/message/date)와
 * 탭 시 화면 라우팅(resourceType/resourceId/kind/messageType)에 필요한 값만 담는다.
 * 별도 로컬 저장은 하지 않는다 — 목록/읽음 상태의 소스 오브 트루스는 서버다.
 *
 * @property id           notificationId (UUID)
 * @property productName  metadata.productName (없으면 title 폴백)
 * @property message      서버 message 문구
 * @property date         createdAt → "yyyy.MM.dd"
 * @property subCategory  metadata.subCategory — 썸네일 이미지 매핑용(DeviceImage)
 * @property resourceType 참조 리소스 유형(없으면 null) — 라우팅 결정
 * @property resourceId   참조 리소스 ID
 * @property kind         알림 종류 이름표 — 특정 종류만 특별 라우팅할 때 사용
 * @property messageType  메시지 유형 — marketing 등 라우팅 결정에 사용
 */
data class AppNotification(
    val id: String,
    val productName: String,
    val message: String,
    val date: String,
    val subCategory: String? = null,
    val resourceType: String? = null,
    val resourceId: String? = null,
    val kind: String? = null,
    val messageType: String? = null,
    val isRead: Boolean = false,
)

/** "2026-06-15T12:00:00" → "2026.06.15" */
private fun String?.toDisplayDate(): String {
    if (this.isNullOrBlank()) return ""
    val datePart = substringBefore('T')
    val parts = datePart.split("-")
    return if (parts.size == 3) "${parts[0]}.${parts[1]}.${parts[2]}" else datePart
}

/**
 * 알림 시간 표시 정책(UX 제안):
 * 1분 미만 "방금 전" / 1시간 미만 "N분 전" / 24시간 미만 "N시간 전" /
 * 7일 미만 "N일 전" / 7일 이상 "yyyy.MM.dd"
 */
private fun String?.toRelativeDisplayTime(): String {
    if (this.isNullOrBlank()) return ""
    val createdMs = runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA).parse(this)?.time
    }.getOrNull() ?: return toDisplayDate()

    val diffMs = (System.currentTimeMillis() - createdMs).coerceAtLeast(0)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour

    return when {
        diffMs < minute -> "방금 전"
        diffMs < hour -> "${diffMs / minute}분 전"
        diffMs < day -> "${diffMs / hour}시간 전"
        diffMs < 7 * day -> "${diffMs / day}일 전"
        else -> toDisplayDate()
    }
}

fun NotificationDto.toAppNotification(): AppNotification = AppNotification(
    id = notificationId,
    productName = metadata["productName"]?.takeIf { it.isNotBlank() }
        ?: title?.takeIf { it.isNotBlank() }
        ?: "알림",
    message = message.orEmpty(),
    date = createdAt.toRelativeDisplayTime(),
    subCategory = metadata["subCategory"],
    resourceType = resourceType,
    resourceId = resourceId,
    kind = kind,
    messageType = messageType,
    isRead = !readAt.isNullOrBlank(),
)
