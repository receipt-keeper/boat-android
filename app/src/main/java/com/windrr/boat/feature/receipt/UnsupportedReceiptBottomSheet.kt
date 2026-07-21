package com.windrr.boat.feature.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 서버가 OCR 분석 응답으로 UNSUPPORTED_RECEIPT 코드를 내려줄 때 노출되는 BottomSheet.
 * 기존에는 Toast로만 안내했으나, 지원 가능한 영수증 카테고리를 안내하는 전용 시트로 변경.
 * AnalysisFailedBottomSheet와 동일한 구조(아이콘/닫기 버튼/안내 박스/CTA)를 재사용한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnsupportedReceiptBottomSheet(
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val colorBrandPrimary = Color(0xFF007AFF)
    val colorGray900 = Color(0xFF111827)
    val colorWhite = Color.White
    val colorGrayBg = Color(0xFFF4F5F7)
    val roundedXl = RoundedCornerShape(12.dp)
    val roundedLg = RoundedCornerShape(8.dp) // CTA 버튼 전용

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorWhite,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp, top = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "닫기",
                        tint = colorGray900
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                ReceiptErrorIcon()

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "현재는 전자제품 영수증만\n지원하고 있어요!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorGray900,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "더 많은 제품 영수증을 지원할 예정이에요!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorBrandPrimary,
                )

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(roundedXl)
                        .background(colorGrayBg)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "현재 등록 가능한 영수증",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorGray900
                    )
                    Spacer(Modifier.height(12.dp))
                    InfoBulletText("IT 제품 (노트북, 휴대폰, 스마트워치, 카메라 등)", colorGray900)
                    Spacer(Modifier.height(6.dp))
                    InfoBulletText("주방가전 (전자레인지, 냉장고, 밥솥, 오븐, 정수기 등)", colorGray900)
                    Spacer(Modifier.height(6.dp))
                    InfoBulletText("세탁·청소 (세탁기, 건조기, 청소기, 로봇청소기 등)", colorGray900)
                    Spacer(Modifier.height(6.dp))
                    InfoBulletText("리빙·냉난방 (에어컨, 선풍기, 공기청정기, 가습기 등)", colorGray900)
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = roundedLg,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorBrandPrimary,
                        contentColor = colorWhite,
                    ),
                ) {
                    Text(
                        text = "영수증 다시 업로드하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
