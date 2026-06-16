package com.windrr.boat.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.windrr.boat.core.log.BoatLog
import java.util.Calendar

object WarrantyAlarmScheduler {

    /** 기본 알림 시점: 만료 30일·7일·1일 전, 당일 */
    val DEFAULT_DAYS_BEFORE = listOf(30, 7, 1, 0)

    /**
     * AS 만료일 기준으로 [alarm.daysBeforeExpiry]일 전 오전 10시에 알람 등록.
     * 이미 지난 시각이면 무시한다.
     */
    fun schedule(context: Context, alarm: WarrantyAlarm) {
        scheduleAt(context, alarm, triggerTime(alarm.expiryDateMillis, alarm.daysBeforeExpiry))
    }

    /**
     * 지금으로부터 [delayMillis] 후에 알람 등록. 테스트·즉시 발송용.
     */
    fun scheduleAfterDelay(context: Context, alarm: WarrantyAlarm, delayMillis: Long = 5_000L) {
        scheduleAt(context, alarm, System.currentTimeMillis() + delayMillis)
    }

    /**
     * 특정 (영수증, 알림 시점) 알람 취소.
     */
    fun cancel(context: Context, receiptId: Long, daysBeforeExpiry: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = buildPendingIntent(
            context,
            WarrantyAlarm(receiptId, "", 0L, daysBeforeExpiry)
        )
        alarmManager.cancel(pendingIntent)
        BoatLog.i("AS 알람 취소: receiptId=$receiptId D-$daysBeforeExpiry")
    }

    /**
     * 특정 영수증의 DEFAULT_DAYS_BEFORE 알람 전부 취소.
     */
    fun cancelAll(context: Context, receiptId: Long) {
        DEFAULT_DAYS_BEFORE.forEach { days -> cancel(context, receiptId, days) }
    }

    private fun scheduleAt(context: Context, alarm: WarrantyAlarm, triggerAtMillis: Long) {
        if (triggerAtMillis <= System.currentTimeMillis()) {
            BoatLog.d("AS 알람 무시 (이미 지남): ${alarm.productName} D-${alarm.daysBeforeExpiry}")
            return
        }

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = buildPendingIntent(context, alarm)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // SCHEDULE_EXACT_ALARM 권한 없음 → 부정확 알람으로 fallback
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            BoatLog.w("정확 알람 권한 없음 — 부정확 알람으로 예약: ${alarm.productName}")
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }

        BoatLog.i("AS 알람 등록: ${alarm.productName} D-${alarm.daysBeforeExpiry} triggerAt=$triggerAtMillis")
    }

    // expiryDateMillis 기준 daysBeforeExpiry일 전 오전 10:00
    private fun triggerTime(expiryDateMillis: Long, daysBeforeExpiry: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = expiryDateMillis
            add(Calendar.DAY_OF_YEAR, -daysBeforeExpiry)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun buildPendingIntent(context: Context, alarm: WarrantyAlarm): PendingIntent {
        val code = requestCode(alarm.receiptId, alarm.daysBeforeExpiry)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WARRANTY_REMINDER
            putExtra(AlarmReceiver.EXTRA_PRODUCT_NAME, alarm.productName)
            putExtra(AlarmReceiver.EXTRA_DAYS_REMAINING, alarm.daysBeforeExpiry)
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, code)
        }
        return PendingIntent.getBroadcast(
            context, code, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // receiptId 최대 21,474,836 * 100 슬롯 — Int 범위 초과 방지
    private fun requestCode(receiptId: Long, daysBeforeExpiry: Int): Int =
        ((receiptId % 21_474_836L) * 100 + daysBeforeExpiry).toInt()
}
