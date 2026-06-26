package com.windrr.boat.feature.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.RoundedLg
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
            // 임시 이미지 placeholder
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedLg)
                    .background(ColorGray300),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.token_empty_image),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGray900,
                )
            }

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
            // 직접 입력 텍스트 링크 (밑줄)
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
                Text(text = stringResource(R.string.analysis_failed_retry), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
