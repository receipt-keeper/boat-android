package com.windrr.boat.feature.notification

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.windrr.boat.core.AppLaunchState
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.repository.UserRepositoryImpl
import com.windrr.boat.ui.component.BoatDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

private const val SNOOZE_DAYS = 30

/**
 * "가입/로그인을 완료한 뒤 앱을 완전히 껐다가 재실행했을 때"만 알림 권한 다이얼로그를 띄운다.
 *
 * - 앱을 처음 켜자마자(또는 이번 프로세스 안에서 방금 로그인/회원가입을 마친 직후)는 노출하지 않는다
 *   → [AppLaunchState.wasLoggedInAtProcessStart]가 true인, 즉 "이미 로그인된 채로 시작된" 콜드 스타트에서만 평가.
 * - 평가는 콜드 스타트당 1회뿐(앱 진입/복귀마다 재확인하던 기존 동작 제거).
 * - "나중에"를 누르면 30일간 다시 노출하지 않되, 알림 권한 자체는 건드리지 않는다.
 * - 다이얼로그에서 알림 권한을 허용하면 서비스의 만료 예정 알림(notificationEnabled/pushEnabled) 설정도 true로 동기화한다.
 *
 * 화면 자체는 렌더링하지 않고 효과 + (필요 시) 다이얼로그만 띄운다. 로그인 후 화면(HomeActivity)에 배치한다.
 */
@Composable
fun NotificationPermissionGate() {
    if (!AppLaunchState.wasLoggedInAtProcessStart) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userDataStore = remember { ApiClient.userDataStore }
    val userRepository = remember {
        UserRepositoryImpl(
            ApiClient.userDataStore,
            ApiClient.userApiService,
            ApiClient.notificationApiService,
            ApiClient.creditsApiService,
        )
    }

    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAlarmRationaleDialog by remember { mutableStateOf(false) }

    fun snoozeForDays() {
        scope.launch {
            val next = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, SNOOZE_DAYS) }.timeInMillis
            userDataStore.updateNextNotifGateDisplayAt(next)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // 다이얼로그에서 알림 권한을 허용한 경우 → 만료 예정 알림 설정도 함께 true로 동기화
            scope.launch { userRepository.updateMe(notificationEnabled = true) }
        } else {
            showSettingsDialog = true
        }
    }

    // 콜드 스타트 1회만 평가 — 이전처럼 앱 진입/복귀(ON_RESUME)마다 재확인하지 않는다.
    LaunchedEffect(Unit) {
        val nextDisplayAt = userDataStore.nextNotifGateDisplayAt.first()
        if (System.currentTimeMillis() < nextDisplayAt) return@LaunchedEffect

        // 1. 기본적인 알림 권한 체크 (POST_NOTIFICATIONS)
        val areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!areNotificationsEnabled) {
            val needsRuntimeRequest = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED

            if (needsRuntimeRequest) {
                showRationaleDialog = true
            } else {
                showSettingsDialog = true
            }
        }
        // 2. 알람 및 리마인더 권한 체크 (SCHEDULE_EXACT_ALARM, Android 12+)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showAlarmRationaleDialog = true
            }
        }
    }

    // 1. Android 13+ 런타임 권한 요청 전 명분(Rationale) 안내
    if (showRationaleDialog) {
        BoatDialog(
            title = "중요 알림을 놓치지 마세요",
            message = "무상 AS 만료일과 중요한 혜택 소식을\n알림으로 가장 먼저 받아볼 수 있습니다.",
            confirmText = "알림 받기",
            onConfirm = {
                showRationaleDialog = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            dismissText = "나중에",
            onDismiss = {
                showRationaleDialog = false
                snoozeForDays()
            },
        )
    }

    // 2. 권한 거절 또는 설정에서 비활성화된 경우 설정 이동 안내
    if (showSettingsDialog) {
        BoatDialog(
            title = "알림 설정이 꺼져 있어요",
            message = "중요한 AS 만료 리마인더를 받으려면\n설정에서 알림을 켜 주세요.",
            confirmText = "설정으로 이동",
            onConfirm = {
                showSettingsDialog = false
                context.openAppNotificationSettings()
            },
            dismissText = "나중에",
            onDismiss = {
                showSettingsDialog = false
                snoozeForDays()
            },
        )
    }

    // 3. 알람 및 리마인더 권한(정확한 알람) 안내
    if (showAlarmRationaleDialog) {
        BoatDialog(
            title = "정확한 알림 설정이 필요해요",
            message = "정해진 시각에 AS 만료 알림을 받으려면\n'알람 및 리마인더' 권한 허용이 필요합니다.",
            confirmText = "설정하러 가기",
            onConfirm = {
                showAlarmRationaleDialog = false
                context.openExactAlarmSettings()
            },
            dismissText = "나중에",
            onDismiss = {
                showAlarmRationaleDialog = false
                snoozeForDays()
            },
        )
    }
}

/** 앱의 시스템 알림 설정 화면 열기 */
private fun Context.openAppNotificationSettings() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
        .onFailure {
            runCatching {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(android.net.Uri.fromParts("package", packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
}

/** 알람 및 리마인더 설정 화면 열기 (Android 12+) */
private fun Context.openExactAlarmSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            .setData(android.net.Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(intent) }
    }
}
