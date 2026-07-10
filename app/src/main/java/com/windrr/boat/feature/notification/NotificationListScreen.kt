package com.windrr.boat.feature.notification

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.R
import com.windrr.boat.feature.receipt.ReceiptDetailActivity
import com.windrr.boat.feature.receipt.ReceiptRegisterActivity
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin20

/**
 * 알림 목록 화면 — 상단 헤더의 종 아이콘으로 진입.
 * 서버(GET /notifications)에서 읽지 않은 알림을 조회해 보여준다.
 * 카드 탭 시 읽음 처리(PATCH) 후 리스트에서 제거하고, resourceType 기준으로 화면 이동한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationListViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.load() }

    // 앱이 처리할 수 있는 resourceType만 라우팅, 그 외/없음은 목록에 머문다.
    // 실제 푸시 탭(PushNotificationRouterActivity)과 동일한 규칙을 쓴다 — resolveNotificationRoute 참고.
    fun route(item: AppNotification) {
        when (val target = resolveNotificationRoute(item.resourceType, item.resourceId, item.kind, item.messageType)) {
            is NotificationRoute.ReceiptDetail ->
                context.startActivity(ReceiptDetailActivity.intent(context, target.receiptId))
            NotificationRoute.ReceiptRegister ->
                context.startActivity(ReceiptRegisterActivity.intent(context))
            NotificationRoute.Home -> Unit // 홈 화면으로 이동 (이미 홈에 있거나 홈으로 이동 처리 필요 시 추가)
            NotificationRoute.None -> Unit
        }
    }

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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(color = ColorBrandPrimary)
                state.notifications.isEmpty() -> Text(
                    text = stringResource(R.string.notif_list_empty),
                    fontSize = 15.sp,
                    color = ColorGray500,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = Margin20, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.notifications, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                viewModel.onNotificationClicked(notification)
                                route(notification)
                            },
                        )
                    }
                }
            }
        }
    }
}
