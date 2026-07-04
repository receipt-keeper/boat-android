package com.windrr.boat.ui.theme

import androidx.compose.ui.unit.dp

// ── Spacing (Margin / Padding) ─────────────────────────────────────────────────
// 16px 이하: 4의 배수
val Margin4  = 4.dp
val Margin8  = 8.dp
val Margin12 = 12.dp
val Margin16 = 16.dp
val Margin20 = 20.dp

// 16px 초과: 8의 배수
val Margin24 = 24.dp
val Margin32 = 32.dp
val Margin40 = 40.dp
val Margin48 = 48.dp
val Margin56 = 56.dp
val Margin64 = 64.dp

/**
 * 플로팅 글래스모피즘 하단 바(BoatFloatingBottomBar)에 가려지지 않도록
 * 홈/목록/마이 탭 스크롤 콘텐츠 맨 아래에 추가로 확보해야 하는 여백.
 * 바 높이(64dp) + 상하 마진(12dp*2) + 시스템 내비게이션 바 여유분 근사치.
 */
val BottomBarClearance = 108.dp
