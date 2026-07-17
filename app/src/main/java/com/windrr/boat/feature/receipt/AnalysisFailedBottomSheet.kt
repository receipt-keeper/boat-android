package com.windrr.boat.feature.receipt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
    errorMessage: String? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 디자인 토큰 색상 매핑 (프로젝트에 정의된 Color Token으로 치환 가능)
    val colorBrandPrimary = Color(0xFF007AFF) // 시그니처 블루
    val colorGray900 = Color(0xFF111827)
    val colorWhite = Color.White
    val colorGrayBg = Color(0xFFF4F5F7) // 안내 박스 옅은 회색
    val roundedXl = RoundedCornerShape(12.dp)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorWhite,
        dragHandle = null, // 💡 디자인 가이드에 따라 상단 바(Handle) 제거
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
        ) {
            // 💡 [교정 1] 우측 상단 X(닫기) 버튼 배치
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

            // 본문 콘텐츠 (좌우 여백 24dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // 💡 [교정 2] 에러 아이콘을 좌측에 배치하고 비율을 스크린샷에 맞게 다듬음
                ReceiptErrorIcon()

                Spacer(Modifier.height(16.dp))

                // 메인 타이틀
                Text(
                    text = errorMessage ?: "영수증 분석에 실패했어요!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorGray900,
                )

                Spacer(Modifier.height(8.dp))

                // 서브 타이틀 (파란색 강조)
                Text(
                    text = "다시 업로드하거나 직접 입력으로 등록해 보세요.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorBrandPrimary,
                )

                Spacer(Modifier.height(24.dp))

                // 💡 [교정 3] 안내 가이드 박스 추가 (회색 배경 + 불릿 리스트)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(roundedXl)
                        .background(colorGrayBg)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "확인해 주세요",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorGray900
                    )
                    Spacer(Modifier.height(12.dp))
                    InfoBulletText("영수증 전체가 보이도록 촬영해 주세요.", colorGray900)
                    Spacer(Modifier.height(6.dp))
                    InfoBulletText("흔들리지 않게 선명하게 찍어주세요.", colorGray900)
                    Spacer(Modifier.height(6.dp))
                    InfoBulletText("그림자가 생기지 않도록 촬영해 주세요.", colorGray900)
                }

                Spacer(Modifier.height(32.dp))

                // 💡 [교정 4] 버튼 1 - 다시 업로드하기 (Primary Filled)
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = roundedXl,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorBrandPrimary,
                        contentColor = colorWhite,
                    ),
                ) {
                    Text(
                        text = "영수증 다시 업로드하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(12.dp))

                // 💡 [교정 5] 버튼 2 - 직접 입력하기 (Outlined 블루)
                OutlinedButton(
                    onClick = onManualInput,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = roundedXl,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorBrandPrimary,
                    ),
                    border = BorderStroke(1.dp, colorBrandPrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "영수증 직접 입력하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/** 불릿(•) 아이템 컴포넌트 — UnsupportedReceiptBottomSheet도 재사용. */
@Composable
fun InfoBulletText(text: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "•",
            fontSize = 14.sp,
            color = color,
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = color,
            lineHeight = 22.sp
        )
    }
}

/** 영수증 아이콘 + 에러 뱃지 (비율 최적화) — UnsupportedReceiptBottomSheet도 재사용. */
@Composable
fun ReceiptErrorIcon() {
    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        // 영수증 본체
        Box(
            modifier = Modifier
                .padding(bottom = 8.dp, end = 8.dp)
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF5AC8FA), Color(0xFF0088FF)),
                    )
                )
                .padding(horizontal = 12.dp, vertical = 14.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth(0.5f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White)
                )
                Box(
                    Modifier
                        .fillMaxWidth(0.9f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                )
                Box(
                    Modifier
                        .fillMaxWidth(0.9f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                )
                Box(
                    Modifier
                        .fillMaxWidth(0.6f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                )
            }
        }

        // 오류 뱃지 (X)
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(2.dp, Color.White, CircleShape)
                .background(Color(0xFFFF3B30), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}