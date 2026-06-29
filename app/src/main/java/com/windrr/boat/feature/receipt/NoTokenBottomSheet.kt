package com.windrr.boat.feature.receipt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandTertiary
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 무료 분석 토큰 소진 시 노출되는 BottomSheet.
 * 충전 / 직접 입력 / 다음에 하기 액션 제공.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoTokenBottomSheet(
    onDismiss: () -> Unit,
    onRecharge: () -> Unit,
    onManualInput: () -> Unit,
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
            Image(
                painter = painterResource(R.drawable.ai_color),
                contentDescription = null,
                modifier = Modifier.size(88.dp),
            )

            Spacer(Modifier.height(Margin20))
            Text(
                text = stringResource(R.string.token_empty_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
            )
            Spacer(Modifier.height(Margin8))
            Text(
                text = stringResource(R.string.token_empty_subtitle),
                fontSize = 14.sp,
                color = ColorGray500,
                textAlign = TextAlign.Center,
            )

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

            Spacer(Modifier.height(Margin8))
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.token_empty_later),
                    fontSize = 14.sp,
                    color = ColorGray500,
                )
            }
        }
    }
}
