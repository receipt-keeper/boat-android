package com.windrr.boat.feature.receipt

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.R
import com.windrr.boat.core.ocr.DeviceImage
import com.windrr.boat.core.util.toPriceString
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandQuinary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorGray100
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Rounded2xl
import com.windrr.boat.ui.theme.RoundedFull
import com.windrr.boat.ui.theme.RoundedLg
import com.windrr.boat.ui.theme.RoundedXl

/** "2024-04-12" → "2024.04.12" */
private fun String?.toDotDate(): String {
    if (this.isNullOrBlank()) return "-"
    val parts = split("-")
    return if (parts.size == 3) "${parts[0]}.${parts[1]}.${parts[2]}" else this
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    receiptId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReceiptDetailViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(receiptId) { viewModel.load(receiptId) }

    Scaffold(
        modifier = modifier,
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
                actions = {
                    // TODO: 수정 화면 연동 (현재는 자리표시)
                    TextButton(onClick = { /* TODO: 수정 */ }) {
                        Text(
                            text = stringResource(R.string.receipt_detail_edit),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorBrandPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorWhite),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(color = ColorBrandPrimary)
                state.receipt == null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error ?: stringResource(R.string.receipt_detail_load_failed),
                            fontSize = 14.sp,
                            color = ColorGray500,
                        )
                        TextButton(onClick = { viewModel.load(receiptId) }) {
                            Text(stringResource(R.string.receipt_list_retry), color = ColorBrandPrimary)
                        }
                    }
                }
                else -> ReceiptDetailContent(receipt = state.receipt!!)
            }
        }
    }
}

@Composable
private fun ReceiptDetailContent(receipt: ReceiptItem) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── 대표 이미지(카테고리/기기) 히어로 카드 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20)
                .padding(top = Margin8)
                .aspectRatio(16f / 9f)
                .clip(Rounded2xl)
                .background(ColorBrandSenary)
                .border(1.dp, ColorBrandQuinary, Rounded2xl),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(DeviceImage.resolve(receipt.category, receipt.subCategory)),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
            )
        }

        Spacer(Modifier.height(Margin24))
        Column(modifier = Modifier.padding(horizontal = Margin20)) {
            DetailField(
                label = stringResource(R.string.manual_product_name),
                value = receipt.itemName,
            )
            DetailField(
                label = stringResource(R.string.manual_purchase_date),
                value = receipt.paymentDate.toDotDate(),
            )
            // 무상 AS 만료일 — 값 우측에 D-day 뱃지
            DetailField(
                label = stringResource(R.string.receipt_detail_expiry),
                value = receipt.expiresOn.toDotDate(),
                trailing = { WarrantyDDayBadge(receipt.warrantyDDay) },
            )

            // 메모
            Spacer(Modifier.height(Margin16))
            FieldLabel(stringResource(R.string.manual_memo))
            Spacer(Modifier.height(Margin8))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedLg)
                    .background(ColorGray50)
                    .padding(Margin16),
            ) {
                val memo = receipt.memo?.takeIf { it.isNotBlank() }
                Text(
                    text = memo ?: stringResource(R.string.receipt_detail_memo_empty),
                    fontSize = 14.sp,
                    color = if (memo != null) ColorGray900 else ColorGray400,
                    lineHeight = 20.sp,
                )
            }
        }

        // ── 실물 영수증 필요 ──
        if (receipt.requiresPhysicalReceipt) {
            SectionBand()
            Column(modifier = Modifier.padding(horizontal = Margin20, vertical = Margin20)) {
                Text(
                    text = stringResource(R.string.receipt_detail_physical_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGray900,
                )
                Spacer(Modifier.height(Margin12))
                Text(
                    text = stringResource(R.string.receipt_detail_physical_desc),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorBrandPrimary,
                    lineHeight = 22.sp,
                )
            }
        }

        // ── 보증 정보 ──
        SectionBand()
        Column(modifier = Modifier.padding(horizontal = Margin20, vertical = Margin20)) {
            Text(
                text = stringResource(R.string.manual_warranty_section),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
            )
            Spacer(Modifier.height(Margin16))
            DetailField(
                label = stringResource(R.string.manual_brand),
                value = receipt.brandName?.takeIf { it.isNotBlank() } ?: "-",
            )
            DetailField(
                label = stringResource(R.string.manual_price),
                value = receipt.totalAmount?.let { "${it.toPriceString()} 원" } ?: "-",
            )
            DetailField(
                label = stringResource(R.string.manual_serial),
                value = receipt.serialNumber?.takeIf { it.isNotBlank() } ?: "-",
            )
        }

        // ── 원본 영수증 ──
        SectionBand()
        Column(modifier = Modifier.padding(vertical = Margin20)) {
            Text(
                text = stringResource(R.string.receipt_detail_original),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
                modifier = Modifier.padding(horizontal = Margin20),
            )
            Spacer(Modifier.height(Margin16))
            if (receipt.receiptFileIds.isEmpty()) {
                Text(
                    text = stringResource(R.string.receipt_detail_original_empty),
                    fontSize = 14.sp,
                    color = ColorGray400,
                    modifier = Modifier.padding(horizontal = Margin20),
                )
            } else {
                // 파일 이미지 서빙 엔드포인트가 확정되면 fileId를 실제 이미지로 교체.
                // TODO: GET 파일 이미지(인증 포함) 연동 — 현재는 플레이스홀더 썸네일
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Margin20),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(receipt.receiptFileIds) { _ ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedXl)
                                .background(ColorGray100)
                                .border(1.dp, ColorGray200, RoundedXl),
                        )
                    }
                }
            }
        }

        // ── 하단 CTA ──
        Spacer(Modifier.height(Margin24))
        val supportUrl = receipt.supportUrl
        androidx.compose.material3.Button(
            onClick = {
                if (!supportUrl.isNullOrBlank()) {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(supportUrl)))
                    }
                }
            },
            enabled = !supportUrl.isNullOrBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20)
                .height(56.dp),
            shape = RoundedXl,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = ColorBrandPrimary,
                contentColor = ColorWhite,
                disabledContainerColor = ColorGray200,
                disabledContentColor = ColorGray500,
            ),
        ) {
            Text(
                text = stringResource(R.string.receipt_detail_support_cta),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(Margin24))
    }
}

/** 라벨(회색) + 값(진한 색) + 하단 구분선. trailing으로 우측 요소(D-day 뱃지 등) 배치. */
@Composable
private fun DetailField(
    label: String,
    value: String,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(vertical = Margin12)) {
        FieldLabel(label)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = ColorGray900,
                modifier = Modifier.weight(1f),
            )
            if (trailing != null) {
                Spacer(Modifier.size(Margin8))
                trailing()
            }
        }
        Spacer(Modifier.height(Margin12))
        HorizontalDivider(thickness = 1.dp, color = ColorGray100)
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, fontSize = 13.sp, color = ColorGray500)
}

/** 섹션 구분 밴드 (연한 회색 8dp) */
@Composable
private fun SectionBand() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(ColorGray50),
    )
}

/** 무상 AS 잔여일 뱃지 — 여유(파랑) / 만료(회색) */
@Composable
private fun WarrantyDDayBadge(dDay: Int?) {
    val (label, color) = when {
        dDay == null || dDay <= 0 -> stringResource(R.string.receipt_expired) to ColorGray400
        else -> "D-$dDay" to ColorBrandPrimary
    }
    Box(
        modifier = Modifier
            .clip(RoundedFull)
            .background(ColorBrandSenary.takeIf { color == ColorBrandPrimary } ?: ColorGray50)
            .border(1.dp, color, RoundedFull)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
