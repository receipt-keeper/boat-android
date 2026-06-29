package com.windrr.boat.feature.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 영수증(OCR) 분석 실패 시 노출되는 BottomSheet.
 * 직접 입력(텍스트 링크) / 다시 업로드 액션 제공.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisFailedBottomSheet(
    onDismiss: () -> Unit,
    onManualInput: () -> Unit,
    onRetry: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorWhite,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20)
                .padding(top = 8.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ReceiptErrorIcon()

            Spacer(Modifier.height(Margin20))
            Text(
                text = stringResource(R.string.analysis_failed_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Margin8))
            Text(
                text = stringResource(R.string.analysis_failed_subtitle),
                fontSize = 14.sp,
                color = ColorGray500,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            Spacer(Modifier.height(Margin20))
            TextButton(onClick = onManualInput) {
                Text(
                    text = stringResource(R.string.analysis_failed_manual),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorBrandPrimary,
                    textDecoration = TextDecoration.Underline,
                )
            }

            Spacer(Modifier.height(Margin8))
            Button(
                onClick = onRetry,
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
                    text = stringResource(R.string.analysis_failed_retry),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** 영수증 아이콘 + 우하단 빨간 X 뱃지 */
@Composable
private fun ReceiptErrorIcon() {
    Box(
        modifier = Modifier.size(96.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        // 영수증 본체 — 파란 그라디언트 카드
        Box(
            modifier = Modifier
                .padding(bottom = 10.dp, end = 10.dp)
                .size(78.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF5AC8FA), Color(0xFF0088FF)),
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                // 영수증 텍스트 라인 표현
                Box(
                    Modifier
                        .fillMaxWidth(0.75f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ColorWhite)
                )
                Box(
                    Modifier
                        .fillMaxWidth(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ColorWhite.copy(alpha = 0.7f))
                )
                Box(
                    Modifier
                        .fillMaxWidth(0.55f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ColorWhite.copy(alpha = 0.7f))
                )
            }
        }

        // 오류 뱃지 — 빨간 원 + 흰 X
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(2.5.dp, ColorWhite, CircleShape)
                .background(Color(0xFFFF3B30), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = ColorWhite,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
