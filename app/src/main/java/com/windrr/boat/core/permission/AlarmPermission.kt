package com.windrr.boat.core.permission

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * 알람/알림 관련 권한 확인 및 요청 인텐트 생성을 담당하는 순수 헬퍼.
 *
 * Context만 있으면 어디서든(ViewModel, Service 등) 권한 상태를 확인할 수 있음.
 * Compose UI에서의 요청 흐름은 [rememberAlarmPermissionState] 사용.
 */
object AlarmPermission {

    /**
     * 알림 표시 권한(POST_NOTIFICATIONS) 보유 여부.
     * Android 12(API 32) 이하는 런타임 권한이 없으므로 항상 true.
     *
     * @param context 권한 확인에 사용할 Context
     * @return 권한 보유 시 true
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * 정확한 시각 알람 예약 가능 여부(SCHEDULE_EXACT_ALARM).
     * Android 11(API 30) 이하는 권한이 필요 없으므로 항상 true.
     *
     * @param context 권한 확인에 사용할 Context
     * @return 정확 알람 예약 가능 시 true
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    /**
     * "알람 및 리마인더" 설정 화면으로 이동하는 인텐트.
     *
     * 정확 알람 권한은 런타임 다이얼로그로 받을 수 없으므로
     * 사용자를 이 설정 화면으로 직접 유도해야 함.
     *
     * @return 정확 알람 설정 화면 인텐트
     */
    fun exactAlarmSettingsIntent(): Intent {
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    }
}

/**
 * 알람/알림 권한 2종의 현재 상태와 요청 동작을 묶은 상태 홀더.
 *
 * 상태 값은 내부 State를 읽으므로 변경 시 자동으로 리컴포지션됨.
 * 정확 알람 rationale 다이얼로그는 [ExactAlarmRationaleDialog]로 렌더링.
 */
@Stable
class AlarmPermissionState internal constructor(
    private val notificationGranted: State<Boolean>,
    private val exactAlarmGranted: State<Boolean>,
    private val rationaleVisible: State<Boolean>,
    /** 알림 권한 요청 (시스템 다이얼로그) */
    val requestNotificationPermission: () -> Unit,
    /** 정확 알람 권한 요청 (rationale 다이얼로그 → 설정 화면) */
    val requestExactAlarmPermission: () -> Unit,
    /** 알림 → 정확 알람 순으로 한 번에 요청 (알림 예약 시점에 호출) */
    val requestAllPermissions: () -> Unit,
    /** rationale 다이얼로그의 "설정으로 이동" 확정 */
    val confirmExactAlarmRationale: () -> Unit,
    /** rationale 다이얼로그 닫기 */
    val dismissExactAlarmRationale: () -> Unit
) {
    /** 알림 표시 권한 보유 여부 */
    val hasNotificationPermission: Boolean
        get() = notificationGranted.value

    /** 정확 알람 예약 가능 여부 */
    val canScheduleExactAlarm: Boolean
        get() = exactAlarmGranted.value

    /** 정확 알람 rationale 다이얼로그 표시 여부 */
    val showExactAlarmRationale: Boolean
        get() = rationaleVisible.value

    /** 알림 + 정확 알람 권한이 모두 허용되었는지 여부 */
    val allGranted: Boolean
        get() = hasNotificationPermission && canScheduleExactAlarm
}

/**
 * 알람/알림 권한 상태를 관찰하고 요청 동작을 제공하는 Compose 매니저.
 *
 * - **알림 권한**: 시스템 권한 다이얼로그
 * - **정확 알람 권한**: rationale 다이얼로그로 안내 후 "알람 및 리마인더" 설정 화면 이동
 * - **포그라운드 복귀(ON_RESUME)**마다 두 권한을 재확인하여 외부 설정 변경 반영
 *
 * 사용 측에서는 반환된 state로 [ExactAlarmRationaleDialog]를 함께 렌더링해야 함.
 *
 * @return 권한 상태와 요청 함수를 담은 [AlarmPermissionState]
 */
@Composable
fun rememberAlarmPermissionState(): AlarmPermissionState {
    val context = LocalContext.current

    val notificationGranted = remember {
        mutableStateOf(AlarmPermission.hasNotificationPermission(context))
    }
    val exactAlarmGranted = remember {
        mutableStateOf(AlarmPermission.canScheduleExactAlarms(context))
    }
    val rationaleVisible = remember { mutableStateOf(false) }
    // 알림 요청 후 이어서 정확 알람 요청까지 진행할지 여부 (시퀀스 플래그)
    val pendingExactAfterNotification = remember { mutableStateOf(false) }

    // 알림 권한 다이얼로그 — 결과 반영 후, 시퀀스 중이면 정확 알람 단계로 진행
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationGranted.value = granted
        if (pendingExactAfterNotification.value) {
            pendingExactAfterNotification.value = false
            if (!AlarmPermission.canScheduleExactAlarms(context)) {
                rationaleVisible.value = true
            }
        }
    }

    // "알람 및 리마인더" 설정 화면 진입용 런처 (재확인은 ON_RESUME에서 일괄 처리)
    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}

    // 앱이 포그라운드로 복귀할 때마다 두 권한을 재확인.
    // 사용자가 앱 밖 시스템 설정에서 권한을 끈 경우까지 반영하기 위함.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationGranted.value = AlarmPermission.hasNotificationPermission(context)
        exactAlarmGranted.value = AlarmPermission.canScheduleExactAlarms(context)
    }

    return remember {
        AlarmPermissionState(
            notificationGranted = notificationGranted,
            exactAlarmGranted = exactAlarmGranted,
            rationaleVisible = rationaleVisible,
            requestNotificationPermission = {
                // 알림 권한은 Android 13+ 에서만 런타임 요청 필요
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            requestExactAlarmPermission = {
                // 시스템 다이얼로그가 없으므로 앱 rationale 다이얼로그로 설정 유도
                if (!AlarmPermission.canScheduleExactAlarms(context)) {
                    rationaleVisible.value = true
                }
            },
            requestAllPermissions = {
                val needNotification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !AlarmPermission.hasNotificationPermission(context)
                when {
                    // 1) 알림 권한부터 다이얼로그로 요청 → 콜백에서 정확 알람 단계로 이어짐
                    needNotification -> {
                        pendingExactAfterNotification.value = true
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    // 2) 알림은 이미 허용 → 정확 알람만 rationale 다이얼로그로 안내
                    !AlarmPermission.canScheduleExactAlarms(context) -> {
                        rationaleVisible.value = true
                    }
                }
            },
            confirmExactAlarmRationale = {
                rationaleVisible.value = false
                exactAlarmLauncher.launch(AlarmPermission.exactAlarmSettingsIntent())
            },
            dismissExactAlarmRationale = {
                rationaleVisible.value = false
            }
        )
    }
}

/**
 * 정확 알람(알람 및 리마인더) 권한 안내 다이얼로그.
 *
 * [AlarmPermissionState.showExactAlarmRationale]가 true일 때만 표시되며,
 * "설정으로 이동" 시 시스템 "알람 및 리마인더" 화면으로 사용자를 인도한다.
 *
 * @param state 알람 권한 상태 홀더
 */
@Composable
fun ExactAlarmRationaleDialog(state: AlarmPermissionState) {
    if (!state.showExactAlarmRationale) return

    AlertDialog(
        onDismissRequest = state.dismissExactAlarmRationale,
        title = { Text("알람 및 리마인더 권한 필요") },
        text = {
            Text(
                "AS 만료일 등 정확한 시각에 알림을 보내려면 " +
                    "'알람 및 리마인더' 권한이 필요해요.\n" +
                    "설정 화면에서 권한을 허용해 주세요."
            )
        },
        confirmButton = {
            TextButton(onClick = state.confirmExactAlarmRationale) {
                Text("설정으로 이동")
            }
        },
        dismissButton = {
            TextButton(onClick = state.dismissExactAlarmRationale) {
                Text("나중에")
            }
        }
    )
}
