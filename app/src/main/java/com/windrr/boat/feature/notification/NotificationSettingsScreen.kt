package com.windrr.boat.feature.notification

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.core.permission.ExactAlarmRationaleDialog
import com.windrr.boat.core.permission.rememberAlarmPermissionState
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin20

/**
 * 알림 설정 화면 — 네이티브 Switch(primary 색상)로 알림/마케팅 수신 동의를 토글.
 * 알림 토글은 실제 권한 보유 여부를 반영하고, 두 토글 값 모두 UserDataStore에 영속화한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val user by viewModel.user.collectAsState()
    val alarmPermission = rememberAlarmPermissionState()
    // 알림 토글은 실제 권한(알림 + 정확 알람) 보유 여부를 반영
    var alarmEnabled by remember { mutableStateOf(alarmPermission.allGranted) }

    // 권한 허용/거부·외부 해제 결과를 토글에 동기화하고 저장 (값이 바뀔 때만 반영되므로 사용자 OFF는 유지됨)
    LaunchedEffect(alarmPermission.allGranted) {
        alarmEnabled = alarmPermission.allGranted
        viewModel.setNotificationEnabled(alarmPermission.allGranted)
    }

    // 설정 변경(PATCH) 실패 시 에러 토스트
    val error by viewModel.error.collectAsState()
    val toastState = rememberBoatToastState()
    LaunchedEffect(error) {
        error?.let {
            toastState.showError(it)
            viewModel.clearError()
        }
    }

    Box(Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = ColorWhite,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.notif_settings_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorGray900,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.common_back),
                            tint = Color.Unspecified,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorWhite),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Spacer(Modifier.height(8.dp))
            ToggleRow(
                label = stringResource(R.string.notif_settings_alarm),
                checked = alarmEnabled,
                onCheckedChange = { wantOn ->
                    when {
                        // 켜려는데 권한 없음 → 권한 요청(알림 → 정확 알람). 결과는 위 LaunchedEffect로 반영
                        wantOn && !alarmPermission.allGranted -> alarmPermission.requestAllPermissions()
                        else -> {
                            alarmEnabled = wantOn
                            viewModel.setNotificationEnabled(wantOn)
                        }
                    }
                },
            )
            ToggleRow(
                label = stringResource(R.string.notif_settings_marketing),
                checked = user.marketingConsent,
                onCheckedChange = { viewModel.setMarketingConsent(it) },
            )
        }
    }

        // 정확 알람 권한 안내 다이얼로그 (설정 화면 유도)
        ExactAlarmRationaleDialog(alarmPermission)

        BoatToastHost(state = toastState)
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Margin20, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = ColorGray900,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ColorWhite,
                checkedTrackColor = ColorBrandPrimary,
                checkedBorderColor = ColorBrandPrimary,
                uncheckedThumbColor = ColorWhite,
                uncheckedTrackColor = ColorGray300,
                uncheckedBorderColor = ColorGray300,
            ),
        )
    }
}
