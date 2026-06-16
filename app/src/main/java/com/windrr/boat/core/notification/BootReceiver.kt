package com.windrr.boat.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.windrr.boat.core.log.BoatLog

/**
 * 기기 재부팅 후 AlarmManager 알람을 재등록하는 리시버.
 *
 * AlarmManager에 등록된 알람은 재부팅 시 모두 사라지므로,
 * BOOT_COMPLETED를 수신해 저장된 알람 목록을 다시 예약해야 한다.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        BoatLog.i("기기 재부팅 감지 — AS 알람 재등록 필요")
        // TODO: 데이터 레이어 완성 후 저장된 WarrantyAlarm 목록을 불러와
        //       WarrantyAlarmScheduler.schedule() 재호출
    }
}
