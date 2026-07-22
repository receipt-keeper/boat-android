package com.windrr.boat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.RoundedFull

/**
 * 카테고리 필터 칩 — 선택 시 brand 채움(흰 글씨), 미선택 시 연한 brand 배경(파란 글씨).
 *
 * 목록 화면의 카테고리 필터 외에도 재사용 가능한 공통 칩 컴포넌트.
 *
 * @param label    칩에 표시할 텍스트
 * @param selected 선택 상태
 * @param onClick  탭 콜백
 */
@Composable
fun BoatFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedFull

    // 💡 [교정 2] 상태에 따른 색상 토큰 분리 (배경, 텍스트, 테두리)
    // 프로젝트 디자인 토큰(ColorBrandPrimary 등)에 맞춰 헥스 코드를 치환하여 사용하십시오.
    val bgColor = if (selected) Color(0xFF3B82F6) else Color.White
    val textColor = if (selected) Color.White else Color(0xFF3B82F6)
    val borderColor = if (selected) Color.Transparent else Color(0xFFBFDBFE) // 옅은 파란색 테두리

    Box(
        modifier = modifier
            // 너비는 허그(콘텐츠에 맞춰 자동), 높이만 37dp 고정
            .height(37.dp)
            .clip(shape)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = Margin20, vertical = Margin8),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            maxLines = 1,
            // 폰트 메트릭의 비대칭 여백(descent가 ascent보다 큼) 때문에 Box를 완벽히 중앙 정렬해도
            // 글자가 살짝 아래로 쏠려 보이던 문제 — includeFontPadding=false + 행간 트림으로 보정.
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            ),
        )
    }
}