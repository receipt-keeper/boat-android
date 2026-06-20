package com.windrr.boat.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light Color Scheme ────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    // Brand
    primary             = ColorBrandPrimary,
    onPrimary           = ColorWhite,
    primaryContainer    = ColorBrandSenary,       // 배너/칩 배경
    onPrimaryContainer  = ColorBrandPrimary,
    secondary           = ColorBrandSecondary,
    onSecondary         = ColorWhite,
    secondaryContainer  = ColorBrandQuaternary,
    onSecondaryContainer = ColorBrandSecondary,
    tertiary            = ColorBrandTertiary,
    onTertiary          = ColorBrandSecondary,
    tertiaryContainer   = ColorBrandQuinary,
    onTertiaryContainer = ColorBrandSecondary,

    // System
    error               = ColorSystemError,
    onError             = ColorWhite,
    errorContainer      = ColorBadgeWarningBg,
    onErrorContainer    = ColorBadgeWarningText,

    // Background / Surface
    background          = ColorWhite,
    onBackground        = ColorGray900,           // 메인 텍스트
    surface             = ColorWhite,
    onSurface           = ColorGray900,           // 메인 텍스트
    surfaceVariant      = ColorGray50,            // 카드/섹션 서브 배경
    onSurfaceVariant    = ColorGray700,           // 서브 텍스트, 보조 아이콘
    surfaceContainer    = ColorGray100,           // 리스트 영역 카드 UI
    surfaceContainerLow = ColorGray50,

    // Border / Divider
    outline             = ColorGray300,           // Input Field, 버튼 테두리
    outlineVariant      = ColorGray200,           // Divider, subtle border
)

// ── Dark Color Scheme ─────────────────────────────────────────────────────────
// 다크 모드 토큰 정의 시 업데이트 예정 — 현재는 Light 값 그대로 사용
private val DarkColorScheme = darkColorScheme(
    primary             = ColorBrandPrimary,
    onPrimary           = ColorWhite,
    primaryContainer    = ColorBrandSecondary,
    onPrimaryContainer  = ColorWhite,
    secondary           = ColorBrandSecondary,
    onSecondary         = ColorWhite,
    error               = ColorSystemError,
    onError             = ColorWhite,
    background          = ColorGray900,
    onBackground        = ColorWhite,
    surface             = ColorGray800,
    onSurface           = ColorWhite,
    surfaceVariant      = ColorGray700,
    onSurfaceVariant    = ColorGray400,
    outline             = ColorGray600,
    outlineVariant      = ColorGray700,
)

@Composable
fun BoatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
