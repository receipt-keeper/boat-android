package com.windrr.boat.ui.theme

import androidx.compose.ui.graphics.Color

// ── Grayscale ─────────────────────────────────────────────────────────────────
val ColorWhite         = Color(0xFFFFFFFF) // 카드, 모달, 섹션 배경, White 서체
val ColorGray10        = Color(0xFFFDFEFF) // Sub background_1
val ColorGray50        = Color(0xFFF5F7FA) // Sub background_2, 토스트 텍스트 색상
val ColorGray100       = Color(0xFFF5F5F5) // Sub background_3, 리스트 영역 카드 UI
val ColorGray200       = Color(0xFFEEEEEE) // Divider, subtle border
val ColorGray300       = Color(0xFFE0E0E0) // Divider, border, 비활성 컬러, Input/버튼 테두리
val ColorGray400       = Color(0xFFBDBDBD) // 보조 아이콘 비활성, 플레이스홀더
val ColorGray500       = Color(0xFF9E9E9E) // 보조 텍스트, 아이콘, 플레이스홀더
val ColorGray600       = Color(0xFF757575) // 본문 텍스트, 부가 아이콘, Divider
val ColorGray700       = Color(0xFF616161) // 서브 텍스트
val ColorGray800       = Color(0xFF212121) // 서브 텍스트
val ColorGray900       = Color(0xFF121212) // Heading, 메인 텍스트, 보조 아이콘 (미트볼, 돋보기 등)
val ColorGray900Alpha80 = Color(0xCC121212) // 보조 아이콘, 시각적 구분 필요한 곳

// ── Brand ─────────────────────────────────────────────────────────────────────
val ColorBrandPrimary    = Color(0xFF0088FF) // 메인 CTA, 강조 헤딩, 아이콘, Checkbox/Radio
val ColorBrandSecondary  = Color(0xFF0E70E3) // 중요도 낮은 CTA
val ColorBrandTertiary   = Color(0xFFC1DAFC) // Border, 배너 색상
val ColorBrandQuaternary = Color(0xFFDAE6F7) // 낮은 CTA, Chip, 배너 배경
val ColorBrandQuinary    = Color(0xFFE6EBF4) // 낮은 CTA, Card Border, 배너 배경
val ColorBrandSenary     = Color(0xFFF0F8FF) // 배너 배경

// ── System ────────────────────────────────────────────────────────────────────
val ColorSystemError   = Color(0xFFFE395B) // 타이핑 에러, 경고 메시지, 에러 아이콘
val ColorSystemSuccess = Color(0xFF3694FF) // 완료 상태, 성공 메시지, 성공 아이콘
val ColorSystemToast   = Color(0xCC212121) // 토스트 배경 (#212121 @ 80%)
val ColorSystemDim     = Color(0x80212121) // 모달 딤 배경 (#212121 @ 50%)

// ── Badge ─────────────────────────────────────────────────────────────────────
// Safe — 안전한 상태 (만료까지 충분히 여유)
val ColorBadgeSafeBg     = Color(0xFFE9F2FF)
val ColorBadgeSafeBorder = Color(0xFFD2E4FF)
val ColorBadgeSafeText   = Color(0xFF0E70E3)

// Warning — 만료 임박 (D-24 이내)
val ColorBadgeWarningBg     = Color(0xFFFFF1F1)
val ColorBadgeWarningBorder = Color(0xFFFFC5C5)
val ColorBadgeWarningText   = Color(0xFFFF3838)

// Expired — 보증기간 만료
val ColorBadgeExpiredBg     = Color(0xFFEEEEEE)
val ColorBadgeExpiredBorder = Color(0xFFE0E0E0)
val ColorBadgeExpiredText   = Color(0xFFBDBDBD)
