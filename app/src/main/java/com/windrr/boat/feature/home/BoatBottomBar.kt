package com.windrr.boat.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin20

// ── 치수 상수 ─────────────────────────────────────────────────────────────────
private val BarHeight = 64.dp            // 알약 바 높이
private val BarCorner = 28.dp            // 알약 바 모서리
private val FabSize = 60.dp              // "+" 원형 버튼 지름 (바 높이와 거의 동일)
private val TabHighlightCorner = 18.dp   // 선택 탭 하이라이트 모서리
private val TabIconSize = 24.dp

/**
 * 글래스모피즘 스타일 플로팅 하단 바.
 * - 흰색(반투명) 둥근 알약 안에 3개 탭(목록/홈/마이)을 균등 배치
 * - 선택 탭은 연한 파랑 하이라이트 + 파란 아이콘/라벨, 바 높이 안에 딱 맞게 담김
 * - 알약과 분리된 검정 원형 "+" 버튼이 오른쪽에 별도로 떠 있음
 *
 * Scaffold의 bottomBar 슬롯이 아니라 콘텐츠 위 오버레이(Box 최상단)로 배치한다.
 * 그래야 리스트가 바 아래로 자연스럽게 스크롤되어 "떠 있는" 느낌이 산다.
 *
 * 실시간 배경 블러는 적용하지 않고 반투명 흰색 + 테두리 + 그림자로 유리질감을 근사한다
 * (minSdk 24에서 실시간 블러는 별도 라이브러리가 필요).
 */
@Composable
fun BoatFloatingBottomBar(
    navController: NavController,
    hazeState: HazeState,
    showAddButton: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // 커스텀 프로스트 유리 — 흰색 틴트 알파를 낮춰 뒤 콘텐츠가 잘 비치게 하고,
    // noiseFactor=0으로 어둡고 거친 그레인 느낌을 제거한다.
    // fallbackTint: 블러 미지원(API 31 미만) 기기에서는 흰색 반투명으로 가독성 확보.
    val glassStyle = HazeStyle(
        backgroundColor = ColorWhite,
        tint = HazeTint(ColorWhite.copy(alpha = 0.12f)),
        blurRadius = 24.dp,
        noiseFactor = 0f,
        fallbackTint = HazeTint(ColorWhite.copy(alpha = 0.72f)),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Margin20, vertical = Margin12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── 알약형 탭 바 ──
        Row(
            modifier = Modifier
                .weight(1f)
                .height(BarHeight)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(BarCorner),
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.18f),
                )
                .clip(RoundedCornerShape(BarCorner))
                .hazeEffect(state = hazeState, style = glassStyle)
                .border(1.dp, ColorWhite.copy(alpha = 0.7f), RoundedCornerShape(BarCorner))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MainTab.entries.forEach { tab ->
                FloatingTabItem(
                    tab = tab,
                    selected = currentRoute == tab.route,
                    modifier = Modifier.weight(1f),
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
                )
            }
        }

        // ── 분리된 "+" 버튼 ──
        if (showAddButton) {
            Spacer(Modifier.width(Margin12))
            AddFloatingButton(hazeState = hazeState, style = glassStyle, onClick = onAddClick)
        }
    }
}

/** 탭 1개 — 선택 시 연한 파랑 하이라이트가 아이콘+라벨을 감싸며 바 안에 담긴다. */
@Composable
private fun FloatingTabItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (selected) ColorBrandPrimary else ColorGray400
    val noRipple = remember { MutableInteractionSource() }

    // 각 탭은 바 높이를 꽉 채우되, 하이라이트는 내부 콘텐츠 크기에 맞춰 그린다.
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(TabHighlightCorner))
            .clickable(interactionSource = noRipple, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(TabHighlightCorner))
                .then(if (selected) Modifier.background(ColorBrandSenary) else Modifier)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(tab.iconRes),
                contentDescription = stringResource(tab.labelRes),
                tint = tint,
                modifier = Modifier.size(TabIconSize),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = stringResource(tab.labelRes),
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = tint,
            )
        }
    }
}

/** 알약과 분리된 원형 영수증 등록 버튼 — 동일한 프로스트 유리 + 진한 "+" 아이콘. */
@Composable
private fun AddFloatingButton(
    hazeState: HazeState,
    style: HazeStyle,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(FabSize)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.18f),
            )
            .clip(CircleShape)
            .hazeEffect(state = hazeState, style = style)
            .border(1.dp, ColorWhite.copy(alpha = 0.7f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = stringResource(R.string.receipt_add),
            tint = ColorGray900,
        )
    }
}
