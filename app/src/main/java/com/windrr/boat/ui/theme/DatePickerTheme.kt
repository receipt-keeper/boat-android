package com.windrr.boat.ui.theme

import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun boatDatePickerColors(): DatePickerColors = DatePickerDefaults.colors(
    // 컨테이너
    containerColor             = ColorWhite,

    // 헤더 ("Mon, Aug 17")
    headlineContentColor       = ColorBrandPrimary,
    // 타이틀 ("Select date")
    titleContentColor          = ColorGray600,

    // 요일 행 (S M T W T F S)
    weekdayContentColor        = ColorGray500,
    // 월/연도 드롭다운 ("August 2025 ▼")
    subheadContentColor        = ColorGray900,
    // < > 네비게이션 화살표
    navigationContentColor     = ColorGray700,

    // 일반 날짜
    dayContentColor            = ColorGray900,
    disabledDayContentColor    = ColorGray300,

    // 선택된 날짜 — 파란 원
    selectedDayContainerColor  = ColorBrandPrimary,
    selectedDayContentColor    = ColorWhite,

    // 오늘 날짜 — 테두리 원
    todayContentColor          = ColorBrandPrimary,
    todayDateBorderColor       = ColorBrandPrimary,

    // 연도 선택 뷰
    yearContentColor                = ColorGray900,
    disabledYearContentColor        = ColorGray300,
    currentYearContentColor         = ColorBrandPrimary,
    selectedYearContainerColor      = ColorBrandPrimary,
    selectedYearContentColor        = ColorWhite,
    disabledSelectedYearContentColor = ColorBrandTertiary,
)
