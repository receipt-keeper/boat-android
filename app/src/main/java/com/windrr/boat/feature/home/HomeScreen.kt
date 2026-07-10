package com.windrr.boat.feature.home

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.R
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.ApiErrorParser
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.data.remote.model.TestPushRequest
import com.windrr.boat.feature.notification.NotificationBadgeViewModel
import com.windrr.boat.feature.receipt.ReceiptDetailActivity
import com.windrr.boat.feature.receipt.ReceiptRegisterActivity
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.RefreshOnResume
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.BoatTheme
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Rounded2xl
import com.windrr.boat.ui.theme.RoundedXl
import kotlinx.coroutines.launch

/**
 * 홈 탭 — 공통 헤더 + AS 만료 예정(가로형)/최근 등록된 영수증(세로형). GET /api/v1/receipts 연동.
 */
@Composable
fun HomeScreen(
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
    val toastState = rememberBoatToastState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.refresh()
        badgeViewModel.refresh()
    }

    // 등록/상세 등 다른 화면에서 홈으로 복귀할 때마다 최신화 (LaunchedEffect는 최초 1회만 실행됨)
    RefreshOnResume {
        viewModel.refresh()
        badgeViewModel.refresh()
    }

    HomeScreenContent(
        state = state,
        hasUnreadNotification = hasUnreadNotification,
        onSearchClick = onSearchClick,
        onNotificationClick = {
            context.startActivity(
                Intent(
                    context,
                    com.windrr.boat.feature.notification.NotificationListActivity::class.java
                )
            )
        },
        onSeeExpiringList = onSeeExpiringList,
        onSeeRecentList = onSeeRecentList,
        onExpiringClick = { item ->
            context.startActivity(ReceiptDetailActivity.intent(context, item.receiptId))
        },
        onRecentClick = { item ->
            context.startActivity(ReceiptDetailActivity.intent(context, item.receiptId))
        },
        onRegisterClick = {
            context.startActivity(Intent(context, ReceiptRegisterActivity::class.java))
        },
        onTestPushClick = { title, body ->
            scope.launch {
                runCatching {
                    ApiClient.exampleApiService.sendTestPush(
                        TestPushRequest(
                            title = title,
                            body = body
                        )
                    )
                }.onSuccess {
                    toastState.show(
                        "발송 완료 (대상 ${it.data.targetedDeviceCount}대, 무효 ${it.data.invalidDeviceCount}대)"
                    )
                }.onFailure {
                    toastState.showError(ApiErrorParser.message(it))
                }
            }
        },
        toastState = toastState,
        modifier = modifier
    )
}

/**
 * 상태(State)만 주입받아 그리는 Stateless 버전의 홈 콘텐츠.
 * 프리뷰에서 다양한 상태를 시뮬레이션하기 위해 사용한다.
 */
@Composable
fun HomeScreenContent(
    state: HomeState,
    hasUnreadNotification: Boolean,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSeeExpiringList: () -> Unit,
    onSeeRecentList: () -> Unit,
    onExpiringClick: (ExpiringWarranty) -> Unit,
    onRecentClick: (RecentReceipt) -> Unit,
    onRegisterClick: () -> Unit,
    onTestPushClick: (String, String) -> Unit,
    toastState: com.windrr.boat.ui.component.BoatToastState,
    modifier: Modifier = Modifier,
) {
    var showTestPushDialog by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3E82F7),
                        Color(0xFFFFFFFF)
                    ),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        // 헤더 + 콘텐츠를 하나의 세로 스크롤로 묶어, 스크롤 시 상단바가 함께 밀려 올라가도록 한다.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .verticalScroll(rememberScrollState())
        ) {
            BoatHeader(
                hasUnreadNotification = hasUnreadNotification,
                onSearchClick = onSearchClick,
                onNotificationClick = onNotificationClick,
                titleColor = ColorWhite,
                iconTint = ColorWhite,
            )
            when {
                // 최초 로딩 중에만 전체 스피너 — 복귀 후 재조회 시엔 기존 내용을 유지한 채 갱신
                state.isLoading && !state.hasLoaded -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorBrandPrimary)
                }

                !state.hasAnyReceipts -> HomeInitialContent(
                    onRegisterClick = onRegisterClick,
                )

                else -> HomeGeneralContent(
                    expiring = state.expiring,
                    expiringTotalCount = state.expiringTotalCount,
                    recent = state.recent,
                    onExpiringMore = onSeeExpiringList,
                    onExpiringClick = onExpiringClick,
                    onRecentMore = onSeeRecentList,
                    onRecentClick = onRecentClick,
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
                onTestPushClick(title, body)
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

/** 초기 홈 — 영수증 등록 CTA 배너 + 가전제품 소모품 배너 */
@Composable
private fun HomeInitialContent(
    onRegisterClick: () -> Unit,
) {
    // 세로 스크롤은 상위(HomeScreenContent)에서 처리하므로 여기선 일반 Column.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Margin20),
    ) {
        Spacer(Modifier.height(Margin8))
        ReceiptRegisterBanner(onClick = onRegisterClick)
        Spacer(Modifier.height(Margin20))
        AccessoryBanner(onClick = { /* TODO: 소모품/액세서리 페이지 연결 */ })
        Spacer(Modifier.height(Margin20))
    }
}

/**
 * 영수증 등록 CTA 배너 — 제목·부제·보보 캐릭터가 하나로 합쳐진 단일 에셋(img_cta_banner)을 그대로 사용.
 * 이미지에 텍스트가 포함되어 있어 화면 낭독기용 설명만 contentDescription으로 별도 제공한다.
 */
@Composable
private fun ReceiptRegisterBanner(onClick: () -> Unit) {
    Image(
        painter = painterResource(R.drawable.img_cta_banner),
        contentDescription = "영수증 등록하기 — 영수증을 등록하고 보증 기간과 구매 정보를 관리해 보세요.",
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .fillMaxWidth()
            .clip(Rounded2xl)
            .clickable(onClick = onClick),
    )
}

/** 가전제품 소모품/액세서리 배너 — 초기 홈/일반 홈(AS 만료 예정 아래) 공용. */
@Composable
fun AccessoryBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedXl)
            .background(Color(0xFFE9F4FF))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_card_popular_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_card_popular_desc),
                fontSize = 13.sp,
                color = Color(0xFF777777),
                lineHeight = 18.sp,
            )
        }
        Image(
            painter = painterResource(R.drawable.img_banner_accessories),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun HomeScreenInitialPreview() {
    BoatTheme {
        HomeScreenContent(
            state = HomeState(
                isLoading = false,
                expiring = emptyList(),
                recent = emptyList(),
                hasAnyReceipts = false
            ),
            hasUnreadNotification = true,
            onSearchClick = {},
            onNotificationClick = {},
            onSeeExpiringList = {},
            onSeeRecentList = {},
            onExpiringClick = {},
            onRecentClick = {},
            onRegisterClick = {},
            onTestPushClick = { _, _ -> },
            toastState = rememberBoatToastState()
        )
    }
}
