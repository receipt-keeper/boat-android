package com.windrr.boat.feature.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.feature.receipt.ReceiptRegisterActivity
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.component.FreeAnalysisBanner
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandQuinary
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 홈 탭 — 공통 헤더 + "영수증 무료 분석" 배너 + 등록/추천 카드(임시 레이아웃).
 */
@Composable
fun HomeScreen(
    freeAnalysisTokens: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        BoatHeader(
            onSearchClick = { /* TODO: 검색 */ },
            onNotificationClick = { /* TODO: 알림 */ },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Margin20),
        ) {
            Spacer(Modifier.height(Margin8))
            FreeAnalysisBanner(remaining = freeAnalysisTokens)

            Spacer(Modifier.height(Margin16))
            HomeCard(
                title = stringResource(R.string.home_card_register_title),
                description = stringResource(R.string.home_card_register_desc),
                minHeight = 360.dp,
                onClick = {
                    context.startActivity(Intent(context, ReceiptRegisterActivity::class.java))
                },
            )

            Spacer(Modifier.height(Margin16))
            HomeCard(
                title = stringResource(R.string.home_card_popular_title),
                description = stringResource(R.string.home_card_popular_desc),
                minHeight = 120.dp,
                onClick = { /* TODO: 인기상품 특가 */ },
            )
            Spacer(Modifier.height(Margin16))
        }
    }
}

/** 홈 카드 (임시 레이아웃) — 추후 디자인 확정 시 교체 */
@Composable
private fun HomeCard(
    title: String,
    description: String,
    minHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedXl)
            .background(ColorWhite)
            .border(1.dp, ColorBrandQuinary, RoundedXl)
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ColorBrandPrimary,
        )
        Spacer(Modifier.height(Margin8))
        Text(
            text = description,
            fontSize = 14.sp,
            color = ColorGray500,
            lineHeight = 20.sp,
        )
    }
}
