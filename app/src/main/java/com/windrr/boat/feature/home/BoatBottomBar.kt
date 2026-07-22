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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
import com.windrr.boat.ui.theme.ColorNavigationTabBg
import com.windrr.boat.ui.theme.ColorGray700
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.PretendardFontFamily

// ── 치수 상수 (디자인 가이드: 높이 62px, 풀 라운드, space-between) ────────────────
private val BarHeight = 62.dp                          // 알약 바 높이
private val BarShape = RoundedCornerShape(percent = 50) // 풀 라운드(스타디움) — 높이 기준 반원 끝
private val FabSize = 62.dp                            // "+" 원형 버튼 지름 (바 높이와 동일)
private val TabHighlightShape = RoundedCornerShape(percent = 50) // 선택 탭 하이라이트도 스타디움
private val TabIconSize = 28.dp // 선택/미선택 공통 — 항상 28dp
private val TabHighlightHeight = 50.dp // 폭은 3등분(weight)으로 화면폭에 맞게 가변, 높이만 고정

/** 탭 라벨 텍스트 스타일 — "caption3 Bold" (Pretendard 700, 10px, 행간 130%, 자간 0) */
private val TabLabelStyle = TextStyle(
    fontFamily = PretendardFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    lineHeight = 13.sp, // 10sp의 130%
    letterSpacing = 0.sp,
)

/**
 * 글래스모피즘 스타일 플로팅 하단 바.
 * - 흰색(반투명) 둥근 알약 안에 3개 탭(목록/홈/마이)을 균등 배치
 * - 선택 탭은 연한 파랑 하이라이트 + 파란 아이콘/라벨, 바 높이 안에 딱 맞게 담김
 * - 알약과 분리된 검정 원형 "+" 버튼이 오른쪽에 별도로 떠 있음
 *
 * Scaffold의 bottomBar 슬롯이 아니라 콘텐츠 위 오버레이(Box 최상단)로 배치한다.
 * 그래야 리스트가 바 아래로 자연스럽게 스크롤되어 "떠 있는" 느낌이 산다.
 */
@Composable
fun BoatFloatingBottomBar(
    navController: NavController,
    hazeState: HazeState,
    showAddButton: Boolean,
    isAddMenuOpen: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTabClick: (MainTab) -> Unit = {},
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
        // FAB로 등록 메뉴를 열면 이 탭 바 전체(FAB 제외)가 배경 콘텐츠처럼 딤 처리되어야 하므로,
        // 탭 Row 위에 딤 오버레이를 별도로 얹을 수 있도록 Box로 감싼다.
        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BarHeight)
                    // Effect: shadow_md3 (디자인 시스템) — X0/Y3/blur15 + X0/Y1/blur7, #000000 10%.
                    // Compose shadow()는 elevation 기반 근사치라 X/Y/blur를 레이어별로 그대로 재현할 수는
                    // 없어 opacity(10%)만 정확히 맞추고, elevation은 두 레이어의 체감 블러에 맞춰 조정했다.
                    .shadow(
                        elevation = 12.dp,
                        shape = BarShape,
                        ambientColor = Color.Black.copy(alpha = 0.10f),
                        spotColor = Color.Black.copy(alpha = 0.10f),
                    )
                    .clip(BarShape)
                    .hazeEffect(state = hazeState, style = glassStyle)
                    .border(1.dp, ColorWhite, BarShape)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween, // 디자인: 양쪽 정렬 space-between
            ) {
                MainTab.entries.forEach { tab ->
                    FloatingTabItem(
                        tab = tab,
                        selected = currentRoute == tab.route,
                        onClick = {
                            onTabClick(tab) // 💡 상위 상태 알림
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    // 시작 목적지까지 pop하되 상태 저장 → 탭별 백스택/상태 보존
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        // 화면폭에 관계없이 3탭이 항상 균등 3등분 — 고정 폭(88dp)을 쓰면 좁은 화면에서
                        // 마지막 탭이 알약 바의 clip 경계 밖으로 밀려 잘려 보이는 문제가 있었다.
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // 등록 메뉴가 열려 있으면 탭 바를 배경 콘텐츠와 동일하게 딤 처리(FAB는 별도 형제 요소라 제외됨).
            // 탭 클릭을 막고, 탭 바를 탭하면 스크림을 탭한 것과 동일하게 메뉴가 닫히도록 한다.
            if (isAddMenuOpen) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(BarShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onAddClick,
                        )
                )
            }
        }

        // ── 분리된 "+" 버튼 ──
        if (showAddButton) {
            Spacer(Modifier.width(Margin12))
            AddFloatingButton(
                hazeState = hazeState,
                style = glassStyle,
                isMenuOpen = isAddMenuOpen,
                onClick = onAddClick
            )
        }
    }
}

/** 탭 1개 — 선택 시 연한 파랑 하이라이트가 아이콘+라벨을 감싸며 바 높이에 꽉 차게 담긴다. */
@Composable
private fun FloatingTabItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 디자인: 선택=fill 아이콘(Secondary 색상 베이크됨)/라벨 Primary, 미선택=아웃라인 아이콘(Gray900) + 다크그레이 라벨(Gray700)
    val iconRes = if (selected) tab.iconResSelected else tab.iconRes
    val iconTint = if (selected) Color.Unspecified else ColorGray900
    val labelColor = if (selected) ColorBrandPrimary else ColorGray700
    val noRipple = remember { MutableInteractionSource() }

    // modifier(호출부에서 .weight(1f)로 3등분된 정확한 폭)를 그대로 받아 outer Box가 그 폭 전체를
    // 클릭 영역으로 쓰고, 안쪽 하이라이트 Column은 그 폭에 꽉 차되 높이만 50dp로 고정한다.
    // → 화면폭이 달라도 항상 정확히 1/3씩 차지해서 특정 탭만 넘치거나 찌그러지는 일이 없다.
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(interactionSource = noRipple, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(TabHighlightHeight)
                .clip(TabHighlightShape)
                .then(if (selected) Modifier.background(ColorNavigationTabBg) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // fill 에셋(선택)은 28×28 캔버스 안에 도형이 ~70%만 차지하도록 이미 여백이 베이크되어
            // 있는 반면, 아웃라인 에셋(미선택)은 viewBox가 도형에 꽉 맞게 크롭돼 있다. 같은
            // Modifier.size(28.dp)를 줘도 실제 그려지는 크기가 서로 달라 보였던 이유 — 아웃라인
            // 쪽에 안쪽 패딩을 줘서 선택 시(fill)와 같은 유효 크기로 맞춘다.
            Icon(
                painter = painterResource(iconRes),
                contentDescription = stringResource(tab.labelRes),
                tint = iconTint,
                modifier = Modifier
                    .size(TabIconSize)
                    .padding(if (selected) 0.dp else 4.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(tab.labelRes),
                style = TabLabelStyle,
                color = labelColor,
            )
        }
    }
}

/** 알약과 분리된 원형 영수증 등록 버튼 — 동일한 프로스트 유리 + 진한 "+" 아이콘. */
@Composable
private fun AddFloatingButton(
    hazeState: HazeState,
    style: HazeStyle,
    isMenuOpen: Boolean,
    onClick: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isMenuOpen) 45f else 0f,
        label = "FabRotation"
    )

    Box(
        modifier = Modifier
            .size(FabSize)
            // Effect: shadow_md3 — 탭 바와 동일한 그림자 스펙으로 통일
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.10f),
            )
            .clip(CircleShape)
            .hazeEffect(state = hazeState, style = style)
            .border(1.dp, ColorWhite, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = stringResource(R.string.receipt_add),
            tint = ColorGray900,
            modifier = Modifier.rotate(rotation)
        )
    }
}
