package com.windrr.boat.feature.notification

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin20

/**
 * 알림 목록 화면 — 상단 헤더의 종 아이콘으로 진입.
 * 사용자가 확인하기 전까지 알림을 목록에 보관해 보여준다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: 실제 알림 데이터 연동 (현재는 임시 데이터)
    val notifications = remember { sampleNotifications() }

    Scaffold(
        containerColor = ColorGray50,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.notif_list_title),
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = Margin20, vertical = 16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            items(notifications, key = { it.id }) { notification ->
                NotificationItem(notification = notification, onClick = { /* TODO: 알림 상세/확인 처리 */ })
            }
        }
    }
}

/** 임시 알림 데이터 3개 (디자인 확인용) */
private fun sampleNotifications(): List<AppNotification> = listOf(
    AppNotification(
        id = 1,
        productName = "IPad Pro 13",
        message = "무상 AS 7일 남았어요! 일주일 뒤에는 무상 AS가 종료돼요.",
        date = "2026.06.15",
    ),
    AppNotification(
        id = 2,
        productName = "IPad Pro 13",
        message = "무상 AS 14일 남았어요! 기간이 지나기 전 영수증을 확인하세요.",
        date = "2026.06.15",
    ),
    AppNotification(
        id = 3,
        productName = "IPad Pro 13",
        message = "무상 AS 30일 남았어요! 만료 전 서비스 센터를 방문해보세요.",
        date = "2026.06.15",
    ),
)
