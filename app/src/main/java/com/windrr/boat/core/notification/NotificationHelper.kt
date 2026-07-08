package com.windrr.boat.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.windrr.boat.MainActivity
import com.windrr.boat.R
import com.windrr.boat.feature.notification.PushNotificationRouterActivity

object NotificationHelper {

    const val CHANNEL_WARRANTY = "warranty_reminder"

    /** 서버(FCM)에서 내려오는 일반 푸시용 채널. 종류가 세분화되면 채널을 추가로 분리한다. */
    const val CHANNEL_GENERAL = "general_push"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val warrantyChannel = NotificationChannel(
            CHANNEL_WARRANTY,
            "AS 기간 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "AS(애프터서비스) 기간 만료 전 미리 알려드립니다"
        }
        val generalChannel = NotificationChannel(
            CHANNEL_GENERAL,
            "일반 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "이벤트 및 서비스 안내 알림"
        }
        manager.createNotificationChannel(warrantyChannel)
        manager.createNotificationChannel(generalChannel)
    }

    /**
     * FCM 등 서버 푸시 표시용 범용 알림.
     * 탭 시 [com.windrr.boat.feature.notification.PushNotificationRouterActivity]를 거쳐
     * 읽음 처리 + resourceType/resourceId 기반 딥링크로 이동한다.
     *
     * @param serverNotificationId 서버 알림 UUID(data.notificationId) — 탭 시 읽음 처리(PATCH)에 사용. 없으면 생략.
     * @param resourceType 참조 리소스 유형(data.resourceType) — 예: "receipt"
     * @param resourceId 참조 리소스 ID(data.resourceId) — 예: 영수증 ID
     * @param kind 알림 종류(data.kind) — 특정 종류만 별도 라우팅할 때 사용
     */
    fun showGeneralPush(
        context: Context,
        title: String,
        body: String,
        notificationId: Int,
        serverNotificationId: String? = null,
        resourceType: String? = null,
        resourceId: String? = null,
        kind: String? = null,
    ) {
        val tapIntent = PushNotificationRouterActivity.intent(
            context = context,
            notificationId = serverNotificationId,
            resourceType = resourceType,
            resourceId = resourceId,
            kind = kind,
        )
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }

    fun showWarrantyReminder(
        context: Context,
        productName: String,
        daysRemaining: Int,
        notificationId: Int
    ) {
        val body = when (daysRemaining) {
            0 -> "오늘 AS 기간이 만료됩니다"
            1 -> "내일 AS 기간이 만료됩니다"
            else -> "AS 기간이 ${daysRemaining}일 남았습니다"
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // TODO: 전용 알림 아이콘(흰색/투명 벡터)으로 교체
        val notification = NotificationCompat.Builder(context, CHANNEL_WARRANTY)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(productName)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }
}
