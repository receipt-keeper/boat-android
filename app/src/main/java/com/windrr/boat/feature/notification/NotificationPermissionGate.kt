package com.windrr.boat.feature.notification

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.windrr.boat.ui.component.BoatDialog

/**
 * 앱 진입/복귀(ON_RESUME)마다 알림 활성화 상태를 확인해, 꺼져 있으면 권한 요청 또는 설정 유도.
 *
 * - Android 13+에서 POST_NOTIFICATIONS 권한이 없으면: 런타임 권한 요청(처음 몇 번은 시스템 다이얼로그).
 * - 권한은 있으나 사용자가 시스템 설정에서 알림을 끈 경우(또는 영구 거부): 앱 알림 설정으로 유도하는 다이얼로그.
 *
 * 화면 자체는 렌더링하지 않고 효과 + (필요 시) 다이얼로그만 띄운다. 로그인 후 화면(HomeActivity)에 배치한다.
 */
@Composable
fun NotificationPermissionGate() {
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // 시스템 다이얼로그에서 거부했거나, 영구 거부라 즉시 false로 온 경우 → 설정 유도
        if (!granted) showSettingsDialog = true
    }

    // 앱이 포그라운드로 올라올 때마다 재확인 (설정에서 껐다가 돌아오는 케이스 포함)
    LifecycleResumeEffect(Unit) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            val needsRuntimeRequest = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            if (needsRuntimeRequest) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // 권한은 있으나 알림이 꺼진 상태(앱 알림 토글 off 등) → 설정으로 안내
                showSettingsDialog = true
            }
        }
        onPauseOrDispose { }
    }

    if (showSettingsDialog) {
        BoatDialog(
            title = "알림이 꺼져 있어요",
            message = "무상 AS 만료 알림 등 중요한 소식을 받으려면 알림을 켜 주세요.",
            confirmText = "알림 켜기",
            onConfirm = {
                showSettingsDialog = false
                context.openAppNotificationSettings()
            },
            dismissText = "나중에",
            onDismiss = { showSettingsDialog = false },
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
            // 일부 기기에서 위 인텐트가 없을 때 앱 상세 설정으로 폴백
            runCatching {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(android.net.Uri.fromParts("package", packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
}
