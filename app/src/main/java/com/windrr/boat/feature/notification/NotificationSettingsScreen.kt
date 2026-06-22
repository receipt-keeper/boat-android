package com.windrr.boat.feature.notification

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
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin20

/**
 * 알림 설정 화면 — 네이티브 Switch(primary 색상)로 알림/마케팅 수신 동의를 토글.
 * TODO: 토글 상태 영속화 + 실제 알림 권한/마케팅 수신 동의 연동
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var alarmEnabled by remember { mutableStateOf(true) }
    var marketingEnabled by remember { mutableStateOf(true) }

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
                onCheckedChange = { alarmEnabled = it },
            )
            ToggleRow(
                label = stringResource(R.string.notif_settings_marketing),
                checked = marketingEnabled,
                onCheckedChange = { marketingEnabled = it },
            )
        }
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
