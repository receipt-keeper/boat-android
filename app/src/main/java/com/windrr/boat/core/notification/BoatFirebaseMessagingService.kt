package com.windrr.boat.core.notification

import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.remote.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * FCM 수신 처리.
 *
 * 디바이스 식별/등록은 FCM registration token 기반이며 [FcmDeviceManager]가 담당한다.
 * TODO: data 페이로드 스펙(알림 타입/딥링크 대상 등)이 확정되면 [onMessageReceived]의 딥링크 라우팅을 확장.
 */
class BoatFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * 토큰이 최초 발급되거나 갱신될 때 호출된다.
     * 로그인 상태(access token 보유)일 때만 서버에 재등록한다 — 미로그인 시엔 인증이 없어 401.
     * (로그인 시엔 HomeActivity 진입에서 register()가 최신 토큰으로 다시 등록한다)
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        BoatLog.i("[FCM] onNewToken — 토큰 갱신 (len=${token.length})")
        scope.launch {
            val loggedIn = ApiClient.tokenDataStore.accessToken.first() != null
            if (loggedIn) {
                FcmDeviceManager.registerToken(token)
            } else {
                BoatLog.i("[FCM] onNewToken — 미로그인 상태라 서버 등록 보류")
            }
        }
    }

    /**
     * 포그라운드에서는 시스템이 알림을 자동으로 그려주지 않으므로 항상 직접 표시한다.
     * (백그라운드/종료 상태에서 "notification" 필드가 있는 메시지는 시스템이 자동 표시하지만,
     *  data-only 메시지는 상태와 무관하게 항상 이 메서드로 전달된다)
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // 1) 수신 자체 확인 — 이 로그가 안 찍히면 메시지가 기기까지 도달 안 함(FID 미등록/토큰 문제/네트워크)
        BoatLog.i(
            "[FCM] onMessageReceived — from=${message.from}, messageId=${message.messageId}, " +
                "notification=${message.notification != null}, dataKeys=${message.data.keys}"
        )

        // 2) 알림 표시 권한/활성화 상태 — Android 13+에서 POST_NOTIFICATIONS 미허용 시 notify()가 무시됨
        val enabled = NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
        BoatLog.i("[FCM] areNotificationsEnabled=$enabled (false면 시스템이 알림을 표시하지 않음 → 권한 확인 필요)")

        // 3) 제목/본문 추출 — notification 우선, 없으면 data
        val title = message.notification?.title ?: message.data["title"]
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        if (title == null) {
            BoatLog.e("[FCM] title이 없어 알림 미표시 — notification/data 모두 title 없음. 페이로드=${message.data}")
            return
        }

        // 4) 실제 표시 시도
        BoatLog.i("[FCM] 알림 표시 시도 — title='$title'")
        NotificationHelper.showGeneralPush(
            context = applicationContext,
            title = title,
            body = body,
            notificationId = System.currentTimeMillis().toInt(),
        )
        BoatLog.i("[FCM] 알림 표시 호출 완료 (notify)")
    }
}
