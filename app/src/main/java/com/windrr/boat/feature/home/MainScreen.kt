package com.windrr.boat.feature.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import com.windrr.boat.R
import com.windrr.boat.feature.mypage.MyPageScreen
import com.windrr.boat.feature.receipt.ReceiptListScreen
import com.windrr.boat.feature.receipt.ReceiptRegisterActivity
import com.windrr.boat.feature.receipt.ReceiptSort
import com.windrr.boat.feature.receipt.ReceiptTab
import com.windrr.boat.feature.receipt.SearchScreen
import com.windrr.boat.ui.component.BoatToastState
import com.windrr.boat.ui.component.UserFeedbackBottomSheet
import com.windrr.boat.ui.component.UserFeedbackViewModel
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite

/**
 * 로그인 이후 메인 화면 호스트.
 * Scaffold(bottomBar + FAB) + NavHost 로 3개 탭(목록/홈/마이)을 전환한다.
 * FAB는 홈 탭에서만 노출되며, 탭 시 영수증 등록 메뉴(ReceiptAddSheet)를 띄운다.
 */
@Composable
fun MainScreen(
    user: com.windrr.boat.data.model.User,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onShowExitToast: () -> Unit,
    toastState: BoatToastState,
    feedbackViewModel: UserFeedbackViewModel,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // 플로팅 바가 뒤 콘텐츠를 실시간 블러(글래스모피즘)로 샘플링하기 위한 상태
    val hazeState = rememberHazeState()
    var showAddMenu by remember { mutableStateOf(false) }
    // 홈 → 목록 탭 진입 시 적용할 inner 탭/정렬 1회성 신호
    var pendingListTab by remember { mutableStateOf<ReceiptTab?>(null) }
    var pendingListSort by remember { mutableStateOf<ReceiptSort?>(null) }

    val showFeedbackSheet by feedbackViewModel.showFeedbackSheet.collectAsState()
    val isFeedbackSubmitting by feedbackViewModel.isSubmitting.collectAsState()
    val msgFeedbackSuccess = stringResource(R.string.feedback_submit_success)
    val msgFeedbackError = stringResource(R.string.feedback_submit_error)

    fun goSearch() {
        navController.navigate("search")
    }

    fun goToListTab() {
        navController.navigate(MainTab.LIST.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val context = LocalContext.current
    var lastBackPressedAt by remember { mutableStateOf(0L) }
    BackHandler {
        when {
            // 1) 등록 메뉴 열려 있으면 닫기
            showAddMenu -> showAddMenu = false
            // 2) 검색 화면이면 이전 화면으로 복귀
            currentRoute == "search" -> navController.popBackStack()
            // 3) 홈 탭이 아니면 홈 탭으로 복귀
            currentRoute != MainTab.HOME.route -> {
                navController.navigate(MainTab.HOME.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            // 3) 홈 탭에서 2초 내 두 번 → 종료, 첫 번째는 안내 토스트
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressedAt < 2_000L) {
                    (context as? Activity)?.finish()
                } else {
                    lastBackPressedAt = now
                    onShowExitToast()
                }
            }
        }
    }

    // 홈/검색은 #F5F7FA 배경(상태바 영역까지) — 목록/마이는 헤더가 흰색이라 흰색 유지
    val systemBackground = if (currentRoute == MainTab.HOME.route || currentRoute == "search") ColorGray50 else ColorWhite

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = systemBackground,
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MainTab.START.route,
                // 이 콘텐츠가 하단 플로팅 바의 블러 소스가 된다.
                // innerPadding(상태바/네비바 inset)은 화면별로 적용한다 —
                // 홈은 상단 그라데이션이 상태바 영역까지 꽉 차도록 top inset을 적용하지 않는다.
                modifier = Modifier.hazeSource(state = hazeState),
            ) {
                composable(MainTab.LIST.route) {
                    ReceiptListScreen(
                        modifier = Modifier.padding(innerPadding),
                        initialTab = pendingListTab,
                        initialSort = pendingListSort,
                        onInitialConsumed = {
                            pendingListTab = null
                            pendingListSort = null
                        },
                        onSearchClick = { goSearch() },
                    )
                }
                composable(MainTab.HOME.route) {
                    HomeScreen(
                        // 상태바 아래까지 그라데이션이 그려지도록 top inset은 생략, 하단 inset만 적용.
                        // (상태바 offset은 HomeScreen 내부에서 statusBars 패딩으로 직접 처리)
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                        onSearchClick = { goSearch() },
                        onSeeExpiringList = {
                            pendingListTab = ReceiptTab.EXPIRING
                            pendingListSort = ReceiptSort.EXPIRING
                            goToListTab()
                        },
                        onSeeRecentList = {
                            pendingListTab = ReceiptTab.ALL
                            pendingListSort = ReceiptSort.RECENT
                            goToListTab()
                        },
                    )
                }
                composable("search") {
                    // 검색 화면은 하단 네비바 inset을 자체 처리하므로 상단 inset만 적용한다.
                    SearchScreen(
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(MainTab.MY.route) {
                    MyPageScreen(
                        modifier = Modifier.padding(innerPadding),
                        name = user.displayName,
                        email = user.email,
                        freeAnalysisTokens = user.freeAnalysisTokensRemaining,
                        profileImageUrl = user.profileImageUrl,
                        onSignOut = onSignOut,
                        onDeleteAccount = onDeleteAccount,
                        onSearchClick = { goSearch() },
                    )
                }
            }
        }

        if (showAddMenu) {
            ReceiptAddSheet(
                onDismiss = { showAddMenu = false },
                onCamera = {
                    showAddMenu = false
                    context.startActivity(
                        ReceiptRegisterActivity.intent(context, ReceiptRegisterActivity.LAUNCH_CAMERA)
                    )
                },
                onGallery = {
                    showAddMenu = false
                    context.startActivity(
                        ReceiptRegisterActivity.intent(context, ReceiptRegisterActivity.LAUNCH_GALLERY)
                    )
                },
            )
        }

        // 플로팅 글래스모피즘 하단 바 — 검색 화면에서는 숨김, 콘텐츠는 이 바 아래로 그대로 스크롤됨
        if (currentRoute != "search") {
            BoatFloatingBottomBar(
                navController = navController,
                hazeState = hazeState,
                showAddButton = true,
                isAddMenuOpen = showAddMenu,
                onAddClick = { showAddMenu = !showAddMenu },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        if (showFeedbackSheet) {
            UserFeedbackBottomSheet(
                onDismiss = { feedbackViewModel.onFeedbackDismissed() },
                onNext = { feedbackViewModel.onFeedbackPostponed() },
                onSubmit = { rating, comment ->
                    feedbackViewModel.submitFeedback(rating, comment) { success ->
                        if (success) {
                            toastState.showSuccess(msgFeedbackSuccess)
                        } else {
                            toastState.showError(msgFeedbackError)
                        }
                    }
                },
                isSubmitting = isFeedbackSubmitting
            )
        }
    }
}
