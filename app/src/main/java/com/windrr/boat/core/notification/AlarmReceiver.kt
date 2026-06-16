package com.windrr.boat.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.windrr.boat.core.log.BoatLog

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_WARRANTY_REMINDER) return

        val productName = intent.getStringExtra(EXTRA_PRODUCT_NAME) ?: return
        val daysRemaining = intent.getIntExtra(EXTRA_DAYS_REMAINING, 0)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        BoatLog.i("AS 알람 수신: $productName D-$daysRemaining")
        NotificationHelper.showWarrantyReminder(context, productName, daysRemaining, notificationId)
    }

    companion object {
        const val ACTION_WARRANTY_REMINDER = "com.windrr.boat.ACTION_WARRANTY_REMINDER"
        const val EXTRA_PRODUCT_NAME = "extra_product_name"
        const val EXTRA_DAYS_REMAINING = "extra_days_remaining"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
