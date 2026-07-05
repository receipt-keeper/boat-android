package com.windrr.boat.core.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.windrr.boat.core.log.BoatLog

/**
 * FCM 수신 처리.
 *
 * 디바이스 식별/등록은 FID 기반이며 [FcmDeviceManager]가 담당한다(FCM 토큰 갱신과 무관).
 * TODO: data 페이로드 스펙(알림 타입/딥링크 대상 등)이 확정되면 [onMessageReceived]의 딥링크 라우팅을 확장.
 */
class BoatFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * 포그라운드에서는 시스템이 알림을 자동으로 그려주지 않으므로 항상 직접 표시한다.
     * (백그라운드/종료 상태에서 "notification" 필드가 있는 메시지는 시스템이 자동 표시하지만,
     *  data-only 메시지는 상태와 무관하게 항상 이 메서드로 전달된다)
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        BoatLog.i("[FCM] 메시지 수신 — data=${message.data}, notification=${message.notification != null}")

        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"].orEmpty()

        NotificationHelper.showGeneralPush(
            context = applicationContext,
            title = title,
            body = body,
            notificationId = System.currentTimeMillis().toInt(),
        )
    }
}
