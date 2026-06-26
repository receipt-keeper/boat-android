package com.windrr.boat.feature.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.windrr.boat.R
import com.windrr.boat.feature.mypage.MyPageScreen
import com.windrr.boat.feature.receipt.ReceiptListScreen
import com.windrr.boat.feature.receipt.ReceiptRegisterActivity
import com.windrr.boat.feature.receipt.ReceiptTab
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
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var showAddMenu by remember { mutableStateOf(false) }
    // 홈 "만료 예정 >" → 목록 탭의 특정 inner 탭으로 진입시키기 위한 1회성 신호
    var pendingListTab by remember { mutableStateOf<ReceiptTab?>(null) }

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
            // 2) 홈 탭이 아니면 홈 탭으로 복귀
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

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = ColorWhite,
            bottomBar = { BoatBottomBar(navController) },
            floatingActionButton = {
                // 홈/목록 탭에서 영수증 등록 FAB 노출 (마이 탭 제외)
                if (currentRoute == MainTab.HOME.route || currentRoute == MainTab.LIST.route) {
                    FloatingActionButton(
                        onClick = { showAddMenu = true },
                        containerColor = ColorGray900,
                        contentColor = ColorWhite,
                        shape = CircleShape,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_plus),
                            contentDescription = stringResource(R.string.receipt_add),
                        )
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MainTab.START.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(MainTab.LIST.route) {
                    ReceiptListScreen(
                        initialTab = pendingListTab,
                        onInitialTabConsumed = { pendingListTab = null },
                    )
                }
                composable(MainTab.HOME.route) {
                    HomeScreen(
                        freeAnalysisTokens = user.freeAnalysisTokensRemaining,
                        onSeeExpiringList = {
                            pendingListTab = ReceiptTab.EXPIRING
                            goToListTab()
                        },
                    )
                }
                composable(MainTab.MY.route) {
                    MyPageScreen(
                        name = user.displayName,
                        email = user.email,
                        profileImageUrl = user.profileImageUrl,
                        onSignOut = onSignOut,
                        onDeleteAccount = onDeleteAccount,
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
    }
}
