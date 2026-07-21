package com.windrr.boat.feature.receipt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.RoundedLg

/** 영수증 등록 완료 화면 — 체크 아이콘 + 안내 문구 + "홈으로 가기"/"보러가기" 버튼. */
@Composable
fun ReceiptRegisterCompleteScreen(
    onGoHome: () -> Unit,
    onViewReceipt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWhite)
            .padding(horizontal = Margin20),
    ) {
        Spacer(Modifier.weight(0.45f))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.icon_complete),
                contentDescription = null,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(Margin20))
            Text(
                text = stringResource(R.string.receipt_register_complete_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Margin8))
            Text(
                text = stringResource(R.string.receipt_register_complete_subtitle),
                fontSize = 15.sp,
                color = ColorGray500,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = Margin20),
            horizontalArrangement = Arrangement.spacedBy(Margin8),
        ) {
            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                shape = RoundedLg,
                border = BorderStroke(1.dp, ColorGray300),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorGray900),
            ) {
                Text(
                    text = stringResource(R.string.receipt_register_complete_home),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Button(
                onClick = onViewReceipt,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                shape = RoundedLg,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorBrandPrimary,
                    contentColor = ColorWhite,
                ),
            ) {
                Text(
                    text = stringResource(R.string.receipt_register_complete_view),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
