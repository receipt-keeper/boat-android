package com.windrr.boat.feature.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.core.notification.WarrantyAlarm
import com.windrr.boat.core.notification.WarrantyAlarmScheduler
import com.windrr.boat.core.permission.ExactAlarmRationaleDialog
import com.windrr.boat.core.permission.rememberAlarmPermissionState

/**
 * 알람/알림 권한 테스트 화면
 *
 * 두 권한(알림 / 정확 알람)의 현재 상태를 표시하고,
 * 진입 시 알림 → 정확 알람 순서로 한 번에 권한을 요청한다.
 *
 * @param onBack 뒤로가기 콜백
 */
@Composable
fun AlarmPermissionTestScreen(
    onBack: () -> Unit
) {
    val alarmPermission = rememberAlarmPermissionState()

    // 정확 알람 권한 안내 다이얼로그 (필요할 때만 표시)
    ExactAlarmRationaleDialog(alarmPermission)

    // 화면 진입 시 알림 → 정확 알람 순서로 한 번에 요청 (1회)
    LaunchedEffect(Unit) {
        alarmPermission.requestAllPermissions()
    }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(text = "← 뒤로")
            }
            Text(
                text = "알람 권한 테스트",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // 권한 상태 표시
            PermissionStatusRow(
                label = "① 알림 권한",
                granted = alarmPermission.hasNotificationPermission
            )
            Spacer(modifier = Modifier.height(8.dp))
            PermissionStatusRow(
                label = "② 알람 및 리마인더",
                granted = alarmPermission.canScheduleExactAlarm
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 알림 → 정확 알람 순서로 한 번에 요청 (실제 알림 예약 시점에 호출할 흐름)
            Button(
                onClick = alarmPermission.requestAllPermissions,
                enabled = !alarmPermission.allGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("권한 한 번에 요청")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 알림 권한만 요청 (다이얼로그)
            OutlinedButton(
                onClick = alarmPermission.requestNotificationPermission,
                enabled = !alarmPermission.hasNotificationPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("① 알림 권한 요청")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 정확 알람 권한만 요청 (rationale 다이얼로그 → 설정 이동)
            OutlinedButton(
                onClick = alarmPermission.requestExactAlarmPermission,
                enabled = !alarmPermission.canScheduleExactAlarm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("② 알람 및 리마인더 권한 요청")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 전체 허용 여부
            if (alarmPermission.allGranted) {
                Text(
                    text = "🎉 모든 알람 권한이 허용되었습니다",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 실제 알람 스케줄러 E2E 테스트 — 5초 후 알림 발송
                OutlinedButton(
                    onClick = {
                        WarrantyAlarmScheduler.scheduleAfterDelay(
                            context = context,
                            alarm = WarrantyAlarm(
                                receiptId = 9999L,
                                productName = "테스트 제품 (갤럭시 S24)",
                                expiryDateMillis = 0L,
                                daysBeforeExpiry = 0
                            ),
                            delayMillis = 5_000L
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("5초 후 테스트 알람 발송", color = Color(0xFF1565C0))
                }
            } else {
                Text(
                    text = "두 권한을 모두 허용해야 알림 예약이 가능합니다",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * 권한 상태 한 줄 표시 (라벨 + 허용 여부 배지)
 *
 * @param label 권한 이름
 * @param granted 허용 여부
 */
@Composable
private fun PermissionStatusRow(
    label: String,
    granted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp)
        Text(
            text = if (granted) "✅ 허용됨" else "❌ 미허용",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (granted) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    }
}
