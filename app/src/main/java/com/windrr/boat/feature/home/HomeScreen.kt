package com.windrr.boat.feature.home

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.BuildConfig
import com.windrr.boat.R
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.ApiErrorParser
import com.windrr.boat.data.remote.model.TestPushRequest
import com.windrr.boat.feature.notification.NotificationBadgeViewModel
import com.windrr.boat.feature.receipt.ReceiptDetailActivity
import com.windrr.boat.feature.receipt.ReceiptRegisterActivity
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.FreeAnalysisBanner
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandTertiary
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.RoundedXl
import kotlinx.coroutines.launch

/**
 * 홈 탭 — 공통 헤더 + AS 만료 예정(가로형)/최근 등록된 영수증(세로형). GET /api/v1/receipts 연동.
 */
@Composable
fun HomeScreen(
    freeAnalysisTokens: Int,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onSeeExpiringList: () -> Unit = {},
    onSeeRecentList: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
    badgeViewModel: NotificationBadgeViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val hasUnreadNotification by badgeViewModel.hasUnread.collectAsState()
    var showTestPushDialog by rememberSaveable { mutableStateOf(false) }
    val toastState = rememberBoatToastState()
    val scope = rememberCoroutineScope()

    // 홈 탭 진입(탭 전환 포함)마다 최신화 — 등록/삭제 후 반영.
    LaunchedEffect(Unit) {
        viewModel.refresh()
        badgeViewModel.refresh()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorGray50), // 홈 기본 배경 #F5F7FA
        ) {
            BoatHeader(
                hasUnreadNotification = hasUnreadNotification,
                onSearchClick = onSearchClick,
                onNotificationClick = {
                    context.startActivity(
                        Intent(context, com.windrr.boat.feature.notification.NotificationListActivity::class.java)
                    )
                },
            )

            // DEBUG 전용 테스트 푸시 버튼
            if (BuildConfig.DEBUG) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Margin20, vertical = 2.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { showTestPushDialog = true }) {
                        Text("[TEST] 푸시", fontSize = 12.sp, color = ColorBrandPrimary)
                    }
                }
            }

            when {
                state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColorBrandPrimary)
                }
                // 등록된 영수증이 하나도 없으면 초기 홈(등록 유도 배너)을 보여준다.
                !state.hasAnyReceipts -> HomeInitialContent(
                    freeAnalysisTokens = freeAnalysisTokens,
                    onRegisterClick = { context.startActivity(Intent(context, ReceiptRegisterActivity::class.java)) },
                )
                else -> HomeGeneralContent(
                    freeAnalysisTokens = freeAnalysisTokens,
                    expiring = state.expiring,
                    recent = state.recent,
                    onExpiringMore = onSeeExpiringList,
                    onExpiringClick = { item ->
                        context.startActivity(ReceiptDetailActivity.intent(context, item.receiptId))
                    },
                    onRecentMore = onSeeRecentList,
                    onRecentClick = { item ->
                        context.startActivity(ReceiptDetailActivity.intent(context, item.receiptId))
                    },
                )
            }
        }

        BoatToastHost(state = toastState)
    }

    if (showTestPushDialog) {
        TestPushDialog(
            onDismiss = { showTestPushDialog = false },
            onConfirm = { title, body ->
                showTestPushDialog = false
                scope.launch {
                    runCatching {
                        ApiClient.exampleApiService.sendTestPush(TestPushRequest(title = title, body = body))
                    }.onSuccess {
                        toastState.show(
                            "발송 완료 (대상 ${it.data.targetedDeviceCount}대, 무효 ${it.data.invalidDeviceCount}대)"
                        )
                    }.onFailure {
                        toastState.showError(ApiErrorParser.message(it))
                    }
                }
            },
        )
    }
}

/** 테스트 푸시 입력 다이얼로그 — 제목/내용 입력 후 발송 (DEBUG 임시 기능) */
@Composable
private fun TestPushDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, body: String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("테스트 알림") }
    var body by rememberSaveable { mutableStateOf("푸시 연결 확인용 테스트 메시지입니다.") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorWhite,
        shape = RoundedXl,
        title = {
            Text("테스트 푸시 발송", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column {
                androidx.compose.material3.OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Margin8))
                androidx.compose.material3.OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("내용") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title.trim(), body.trim()) },
                enabled = title.isNotBlank() && body.isNotBlank(),
            ) { Text("발송", color = ColorBrandPrimary, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소", color = ColorGray500) }
        },
    )
}

/** 초기 홈 — 무료 분석 배너 + 등록 배너 + AS 배너 */
@Composable
private fun HomeInitialContent(
    freeAnalysisTokens: Int,
    onRegisterClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Margin20),
    ) {
        Spacer(Modifier.height(Margin8))
        FreeAnalysisBanner(remaining = freeAnalysisTokens)

        Spacer(Modifier.height(Margin16))
        ReceiptRegisterBanner(onClick = onRegisterClick)

        Spacer(Modifier.height(Margin16))
        RepairServiceBanner(onClick = { /* TODO: 수리 서비스 연결 */ })

        Spacer(Modifier.height(Margin16))
    }
}

/** 영수증 등록 배너 — 파란 배경 + 영수증 이미지 */
@Composable
private fun ReceiptRegisterBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 360.dp)
            .clip(RoundedXl)
            .background(ColorBrandPrimary)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(R.drawable.img_receipt_upload),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = Margin20, top = 24.dp, end = Margin20),
        ) {
            Text(
                text = stringResource(R.string.home_card_register_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ColorWhite,
                lineHeight = 36.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_card_register_desc),
                fontSize = 14.sp,
                color = ColorWhite,
                lineHeight = 20.sp,
            )
        }
    }
}

/** 가전제품 AS 배너 — 흰 배경 + Brand/Tertiary 1dp 테두리 + 수리 서비스 이미지 */
@Composable
private fun RepairServiceBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clip(RoundedXl)
            .background(ColorWhite)
            .border(1.dp, ColorBrandTertiary, RoundedXl)
            .clickable(onClick = onClick)
            .padding(horizontal = Margin20, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_card_repair_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorBrandPrimary,
                maxLines = 1,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_card_repair_desc),
                fontSize = 13.sp,
                color = ColorGray500,
                lineHeight = 18.sp,
            )
        }
        Spacer(Modifier.width(Margin16))
        Image(
            painter = painterResource(R.drawable.img_banner_repair_service),
            contentDescription = null,
            modifier = Modifier.size(88.dp),
            contentScale = ContentScale.Fit,
        )
    }
}
