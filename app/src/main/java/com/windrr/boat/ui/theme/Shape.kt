package com.windrr.boat.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ── Radius 토큰 ───────────────────────────────────────────────────────────────
val RoundedSm   = RoundedCornerShape(4.dp)   // rounded_sm
val RoundedMd   = RoundedCornerShape(6.dp)   // rounded_md
val RoundedLg   = RoundedCornerShape(8.dp)   // rounded_lg
val RoundedXl   = RoundedCornerShape(12.dp)  // rounded_xl
val Rounded2xl  = RoundedCornerShape(16.dp)  // rounded_2xl
val Rounded3xl  = RoundedCornerShape(24.dp)  // rounded_3xl
val RoundedFull = RoundedCornerShape(999.dp) // rounded_full

// ── Material3 Shapes 매핑 ─────────────────────────────────────────────────────
val BoatShapes = Shapes(
    extraSmall = RoundedSm,   // 4dp  — 소형 칩, 툴팁
    small      = RoundedMd,   // 6dp  — 소형 버튼, 뱃지
    medium     = RoundedLg,   // 8dp  — 카드, 다이얼로그
    large      = RoundedXl,   // 12dp — 바텀시트, 모달
    extraLarge = Rounded2xl,  // 16dp — 대형 카드
)
