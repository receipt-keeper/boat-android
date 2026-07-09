package com.windrr.boat.feature.receipt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandTertiary
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.Rounded2xl
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 무료 분석 토큰 소진 시 노출되는 BottomSheet.
 *
 * [canRecharge] true — 수령 가능한 충전 프로모션이 있을 때. 좌측 정렬 레이아웃으로
 * 안내 박스 + "충전하기"/"직접 입력하기" 2버튼을 보여준다.
 * [canRecharge] false — 이미 이번 달 수령했거나 노출할 프로모션이 없을 때. 중앙 정렬
 * 레이아웃으로 "직접 입력하기" 단일 버튼만 보여준다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoTokenBottomSheet(
    onDismiss: () -> Unit,
    onRecharge: () -> Unit,
    onManualInput: () -> Unit,
    canRecharge: Boolean = true,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorWhite,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = Margin24),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, end = 8.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.receipt_detail_menu_close),
                        tint = ColorGray900,
                    )
                }
            }

            if (canRecharge) {
                NoTokenPromoContent(onRecharge = onRecharge, onManualInput = onManualInput)
            } else {
                NoTokenSimpleContent(onManualInput = onManualInput)
            }
        }
    }
}

/** 충전 프로모션이 있을 때 — 좌측 정렬 + 유의사항 박스 + 충전/직접입력 2버튼 */
@Composable
private fun NoTokenPromoContent(
    onRecharge: () -> Unit,
    onManualInput: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = Margin20)) {
        Icon(
            painter = painterResource(R.drawable.ai_color),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(40.dp),
        )

        Spacer(Modifier.height(Margin16))
        Text(
            text = stringResource(R.string.token_empty_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorGray900,
            lineHeight = 28.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.token_empty_subtitle),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorBrandPrimary,
        )

        Spacer(Modifier.height(Margin20))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Rounded2xl)
                .background(ColorGray50)
                .padding(Margin16),
        ) {
            Text(
                text = stringResource(R.string.token_empty_notice_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "•",
                    fontSize = 13.sp,
                    color = ColorGray600,
                    modifier = Modifier.padding(end = 6.dp, top = 1.dp),
                )
                Text(
                    text = stringResource(R.string.token_empty_notice_body),
                    fontSize = 13.sp,
                    color = ColorGray600,
                    lineHeight = 19.sp,
                )
            }
        }

        Spacer(Modifier.height(Margin24))
        Button(
            onClick = onRecharge,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedXl,
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorBrandPrimary,
                contentColor = ColorWhite,
            ),
        ) {
            Text(
                text = stringResource(R.string.token_empty_recharge),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(Margin8))
        OutlinedButton(
            onClick = onManualInput,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedXl,
            border = BorderStroke(1.dp, ColorBrandTertiary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorBrandPrimary),
        ) {
            Text(
                text = stringResource(R.string.token_empty_manual),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** 충전 프로모션이 없을 때 — 중앙 정렬 + 직접입력 단일 버튼 */
@Composable
private fun NoTokenSimpleContent(onManualInput: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Margin20),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ai_color),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(56.dp),
        )

        Spacer(Modifier.height(Margin16))
        Text(
            text = stringResource(R.string.token_empty_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorGray900,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Margin8))
        Text(
            text = stringResource(R.string.token_empty_subtitle_no_promo),
            fontSize = 14.sp,
            color = ColorGray500,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )

        Spacer(Modifier.height(Margin24))
        Button(
            onClick = onManualInput,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedXl,
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorBrandPrimary,
                contentColor = ColorWhite,
            ),
        ) {
            Text(
                text = stringResource(R.string.token_empty_manual),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
