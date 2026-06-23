package com.windrr.boat.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorBrandTertiary
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.RoundedFull
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 홈 탭 — 공통 헤더 + "영수증 무료 분석" 배너 (이후 등록/추천 카드 등으로 확장 예정)
 */
@Composable
fun HomeScreen(
    freeAnalysisTokens: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BoatHeader(
            onSearchClick = { /* TODO: 검색 */ },
            onNotificationClick = { /* TODO: 알림 */ },
        )
        FreeAnalysisBanner(
            remaining = freeAnalysisTokens,
            modifier = Modifier.padding(horizontal = Margin20, vertical = Margin8),
        )
    }
}

/** "영수증 무료 분석" 배너 — 좌측 스파클 + 라벨, 우측 "잔여 N회" 뱃지 */
@Composable
private fun FreeAnalysisBanner(
    remaining: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedXl)
            .background(ColorBrandSenary)
            .border(1.dp, ColorBrandTertiary, RoundedXl)
            .padding(horizontal = Margin16, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ai_color),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Margin8))
        Text(
            text = stringResource(R.string.home_free_analysis),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorGray900,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.home_free_analysis_remaining, remaining),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ColorWhite,
            modifier = Modifier
                .clip(RoundedFull)
                .background(ColorBrandPrimary)
                .padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}
