package com.windrr.boat.feature.receipt

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.component.FreeAnalysisBanner
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandTertiary
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 영수증 기기 등록 화면 — 무료 분석 배너 + 업로드 영역(placeholder) + 카메라/갤러리 + 분석 시작.
 * 영수증 미선택 상태에서는 "영수증 분석 시작하기"가 비활성.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptRegisterScreen(
    freeAnalysisTokens: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: 업로드된 영수증 이미지 목록 (현재는 빈 상태)
    val uploadedReceipts = emptyList<Unit>()

    Scaffold(
        containerColor = ColorWhite,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.common_back),
                            tint = Color.Unspecified,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorWhite),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Margin20),
        ) {
            Spacer(Modifier.height(Margin8))
            FreeAnalysisBanner(remaining = freeAnalysisTokens)

            Spacer(Modifier.height(Margin24))
            Text(
                text = stringResource(R.string.receipt_register_uploaded),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ColorGray900,
            )

            Spacer(Modifier.height(Margin16))
            // 업로드 영역 — 빈 상태면 점선 placeholder 1칸
            if (uploadedReceipts.isEmpty()) {
                UploadPlaceholder()
            }
            // TODO: 업로드된 영수증 썸네일 그리드

            Spacer(Modifier.weight(1f))

            OutlineActionButton(
                icon = R.drawable.ic_camera,
                label = R.string.receipt_register_camera,
                onClick = { /* TODO: 카메라 촬영 */ },
            )
            Spacer(Modifier.height(Margin12))
            OutlineActionButton(
                icon = R.drawable.ic_gallery,
                label = R.string.receipt_register_gallery,
                onClick = { /* TODO: 갤러리 선택 */ },
            )

            Spacer(Modifier.height(Margin24))
            Button(
                onClick = { /* TODO: 영수증 분석 시작 */ },
                enabled = uploadedReceipts.isNotEmpty(), // 영수증 선택 전 비활성
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedXl,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorBrandPrimary,
                    contentColor = ColorWhite,
                    disabledContainerColor = ColorGray200,
                    disabledContentColor = ColorGray500,
                ),
            ) {
                Text(
                    text = stringResource(R.string.receipt_register_analyze),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(Margin16))
        }
    }
}

/** 점선 테두리 업로드 placeholder */
@Composable
private fun UploadPlaceholder() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .drawBehind {
                drawRoundRect(
                    color = ColorGray300,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_gallery),
            contentDescription = null,
            tint = ColorGray400,
            modifier = Modifier.size(32.dp),
        )
    }
}

/** 카메라/갤러리 외곽선 버튼 (연한 brand 보더 + brand 아이콘/텍스트) */
@Composable
private fun OutlineActionButton(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedXl)
            .border(1.dp, ColorBrandTertiary, RoundedXl)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = ColorBrandPrimary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Margin8))
        Text(
            text = stringResource(label),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = ColorBrandPrimary,
        )
    }
}
