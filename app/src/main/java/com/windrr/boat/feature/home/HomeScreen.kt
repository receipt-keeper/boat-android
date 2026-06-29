package com.windrr.boat.feature.home

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.feature.receipt.ReceiptRegisterActivity
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.component.FreeAnalysisBanner
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandTertiary
import com.windrr.boat.ui.theme.ColorGray50
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
    onSearchClick: () -> Unit = {},
    onSeeExpiringList: () -> Unit = {},
    onSeeRecentList: () -> Unit = {},
) {
    val context = LocalContext.current
    // 임시: 초기 홈 ↔ 일반 홈 전환 (백엔드 데이터 유무에 따른 화면 확인용)
    var isGeneralHome by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorGray50), // 홈 기본 배경 #F5F7FA
    ) {
        BoatHeader(
            onSearchClick = onSearchClick,
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
                freeAnalysisTokens = freeAnalysisTokens,
                expiring = remember { sampleExpiringWarranties() },
                recent = remember { sampleRecentReceipts() },
                onExpiringMore = onSeeExpiringList,
                onRecentMore = onSeeRecentList,
            )
        } else {
            HomeInitialContent(
                freeAnalysisTokens = freeAnalysisTokens,
                onRegisterClick = { context.startActivity(Intent(context, ReceiptRegisterActivity::class.java)) },
            )
        }
    }
}

/** 초기 홈 — 무료 분석 배너 + 등록 배너 + AS 배너 */
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
        ReceiptRegisterBanner(onClick = onRegisterClick)

        Spacer(Modifier.height(Margin16))
        RepairServiceBanner(onClick = { /* TODO: 수리 서비스 연결 */ })

        Spacer(Modifier.height(Margin16))
    }
}

/** 영수증 등록 배너 — 파란 배경 + 영수증 이미지 */
@Composable
private fun ReceiptRegisterBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 360.dp)
            .clip(RoundedXl)
            .background(ColorBrandPrimary)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(R.drawable.img_receipt_upload),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = Margin20, top = 24.dp, end = Margin20),
        ) {
            Text(
                text = stringResource(R.string.home_card_register_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ColorWhite,
                lineHeight = 36.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_card_register_desc),
                fontSize = 14.sp,
                color = ColorWhite,
                lineHeight = 20.sp,
            )
        }
    }
}

/** 가전제품 AS 배너 — 흰 배경 + Brand/Tertiary 1dp 테두리 + 수리 서비스 이미지 */
@Composable
private fun RepairServiceBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clip(RoundedXl)
            .background(ColorWhite)
            .border(1.dp, ColorBrandTertiary, RoundedXl)
            .clickable(onClick = onClick)
            .padding(horizontal = Margin20, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_card_repair_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorBrandPrimary,
                maxLines = 1,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_card_repair_desc),
                fontSize = 13.sp,
                color = ColorGray500,
                lineHeight = 18.sp,
            )
        }
        Spacer(Modifier.width(Margin16))
        Image(
            painter = painterResource(R.drawable.img_banner_repair_service),
            contentDescription = null,
            modifier = Modifier.size(88.dp),
            contentScale = ContentScale.Fit,
        )
    }
}
