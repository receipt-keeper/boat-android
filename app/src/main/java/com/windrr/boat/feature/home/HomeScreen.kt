package com.windrr.boat.feature.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 홈 탭 — 공통 헤더 + (임시 토글) 초기 홈 / 일반 홈(데이터 있음) 전환.
 */
@Composable
fun HomeScreen(
    freeAnalysisTokens: Int,
    modifier: Modifier = Modifier,
    onSeeExpiringList: () -> Unit = {},
) {
    val context = LocalContext.current
    // 임시: 초기 홈 ↔ 일반 홈 전환 (백엔드 데이터 유무에 따른 화면 확인용)
    var isGeneralHome by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        BoatHeader(
            onSearchClick = { /* TODO: 검색 */ },
            onNotificationClick = {
                context.startActivity(
                    Intent(context, com.windrr.boat.feature.notification.NotificationListActivity::class.java)
                )
            },
        )

        // 임시 전환 토글 (개발 확인용)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20, vertical = 2.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.home_toggle_general), fontSize = 12.sp, color = ColorGray500)
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = isGeneralHome,
                onCheckedChange = { isGeneralHome = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ColorWhite,
                    checkedTrackColor = ColorBrandPrimary,
                    checkedBorderColor = ColorBrandPrimary,
                    uncheckedThumbColor = ColorWhite,
                    uncheckedTrackColor = ColorGray300,
                    uncheckedBorderColor = ColorGray300,
                ),
            )
        }

        if (isGeneralHome) {
            HomeGeneralContent(
                expiring = remember { sampleExpiringWarranties() },
                recent = remember { sampleRecentReceipts() },
                onExpiringMore = onSeeExpiringList,
            )
        } else {
            HomeInitialContent(
                freeAnalysisTokens = freeAnalysisTokens,
                onRegisterClick = { context.startActivity(Intent(context, ReceiptRegisterActivity::class.java)) },
            )
        }
    }
}

/** 초기 홈 — 무료 분석 배너 + 등록/추천 카드(임시 레이아웃) */
@Composable
private fun HomeInitialContent(
    freeAnalysisTokens: Int,
    onRegisterClick: () -> Unit,
) {
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
            onClick = onRegisterClick,
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
        Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ColorBrandPrimary)
        Spacer(Modifier.height(Margin8))
        Text(text = description, fontSize = 14.sp, color = ColorGray500, lineHeight = 20.sp)
    }
}
