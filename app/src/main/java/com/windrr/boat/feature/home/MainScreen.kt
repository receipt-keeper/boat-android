package com.windrr.boat.feature.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.windrr.boat.feature.mypage.MyPageScreen
import com.windrr.boat.feature.receipt.ReceiptListScreen
import com.windrr.boat.ui.theme.ColorWhite

/**
 * 로그인 이후 메인 화면 호스트.
 * Scaffold(bottomBar) + NavHost 로 3개 탭(목록/홈/마이)을 전환한다.
 */
@Composable
fun MainScreen(
    displayName: String?,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = ColorWhite,
        bottomBar = { BoatBottomBar(navController) },
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
}
