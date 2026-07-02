package com.windrr.boat.feature.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.core.util.toPriceString
import com.windrr.boat.data.remote.model.OcrData
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray100
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20

/**
 * OCR 분석 결과 raw 표시 화면 (임시). 키-값 나열 + 경고/검토 필요 여부.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrResultScreen(
    result: OcrData,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = ColorWhite,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "OCR 분석 결과",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorGray900,
                    )
                },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Margin20),
        ) {
            Spacer(Modifier.height(Margin12))

            ResultRow("대표 항목", result.itemName)
            ResultRow("브랜드", result.brandName)
            ResultRow("구매처", result.paymentLocation)
            ResultRow("구매일", result.paymentDate)
            ResultRow("금액", result.totalAmount?.let { "${it.toPriceString()}원" })
            ResultRow("AS 기간", result.periodMonths?.let { "${it}개월" })
            ResultRow("만료일", result.expiresOn)
            ResultRow("카테고리", result.category)
            ResultRow("검토 필요", if (result.needsReview) "예" else "아니오")

            if (result.warnings.isNotEmpty()) {
                Spacer(Modifier.height(Margin16))
                Text(
                    text = "경고",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorGray500,
                )
                Spacer(Modifier.height(Margin12))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorGray100)
                        .padding(Margin16),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    result.warnings.forEach { warning ->
                        Text(
                            text = "• $warning",
                            fontSize = 14.sp,
                            color = ColorGray900,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Margin20))
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = ColorGray500,
            modifier = Modifier.width(88.dp),
        )
        Spacer(Modifier.width(Margin16))
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "-",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (value.isNullOrBlank()) ColorGray500 else ColorBrandPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}
