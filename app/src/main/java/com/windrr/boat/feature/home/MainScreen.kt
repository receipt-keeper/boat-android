package com.windrr.boat.feature.home

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.windrr.boat.R
import com.windrr.boat.feature.mypage.MyPageScreen
import com.windrr.boat.feature.receipt.ReceiptListScreen
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite

/**
 * 로그인 이후 메인 화면 호스트.
 * Scaffold(bottomBar + FAB) + NavHost 로 3개 탭(목록/홈/마이)을 전환한다.
 * FAB는 홈 탭에서만 노출되며, 탭 시 영수증 등록 메뉴(ReceiptAddSheet)를 띄운다.
 */
@Composable
fun MainScreen(
    displayName: String?,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var showAddMenu by remember { mutableStateOf(false) }

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
                composable(MainTab.LIST.route) { ReceiptListScreen() }
                composable(MainTab.HOME.route) { HomeScreen(displayName = displayName) }
                composable(MainTab.MY.route) {
                    MyPageScreen(onSignOut = onSignOut, onDeleteAccount = onDeleteAccount)
                }
            }
        }

        if (showAddMenu) {
            ReceiptAddSheet(
                onDismiss = { showAddMenu = false },
                onCamera = { showAddMenu = false /* TODO: 카메라 촬영 → 영수증 등록 */ },
                onGallery = { showAddMenu = false /* TODO: 갤러리 선택 → 영수증 등록 */ },
            )
        }
    }
}
