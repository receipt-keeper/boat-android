package com.windrr.boat.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.windrr.boat.MainActivity
import com.windrr.boat.R

object NotificationHelper {

    const val CHANNEL_WARRANTY = "warranty_reminder"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_WARRANTY,
            "AS 기간 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "AS(애프터서비스) 기간 만료 전 미리 알려드립니다"
        }
        manager.createNotificationChannel(channel)
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
