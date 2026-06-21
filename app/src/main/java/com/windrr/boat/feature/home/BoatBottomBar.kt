package com.windrr.boat.feature.home

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorWhite

/**
 * 하단 Bottom Navigation 바.
 * 선택 탭은 ColorBrandPrimary, 비활성 탭은 ColorGray400 으로 tint(아이콘에 박힌 색을 덮어씀).
 * 탭 전환은 백스택/상태 보존 표준 패턴(saveState/restoreState/launchSingleTop)을 사용한다.
 */
@Composable
fun BoatBottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(containerColor = ColorWhite) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            // 시작 목적지까지 pop하되 상태 저장 → 탭별 백스택/상태 보존
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(tab.iconRes),
                        contentDescription = stringResource(tab.labelRes),
                    )
                },
                label = { Text(stringResource(tab.labelRes), fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ColorBrandPrimary,
                    selectedTextColor = ColorBrandPrimary,
                    unselectedIconColor = ColorGray400,
                    unselectedTextColor = ColorGray400,
                    indicatorColor = Color.Transparent, // M3 기본 알약형 인디케이터 제거
                ),
            )
        }
    }
}
