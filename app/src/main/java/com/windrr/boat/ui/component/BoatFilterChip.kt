package com.windrr.boat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandQuaternary
import com.windrr.boat.ui.theme.ColorWhite

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
    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        color = if (selected) ColorWhite else ColorBrandPrimary,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) ColorBrandPrimary else ColorBrandQuaternary)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    )
}
