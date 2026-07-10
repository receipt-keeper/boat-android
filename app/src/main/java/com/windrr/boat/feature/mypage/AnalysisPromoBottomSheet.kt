package com.windrr.boat.feature.mypage

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.windrr.boat.R

/** "영수증 분석" 배너 "보기" 클릭 시 뜨는 무료 분석 이벤트 안내 BottomSheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisPromoBottomSheet(
    onDismiss: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White, // ColorWhite
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 💡 [교정 1] X버튼 상단 압착 방지(top 24.dp) 및 하단 안드로이드 시스템 제스처 바 침범 방어
                .padding(top = 24.dp, bottom = 24.dp)
                .navigationBarsPadding(),
        ) {
            // 💡 [교정 2] 우측 20dp 텍스트 마진선과 시각적으로 정렬하되, 48dp 터치 타겟은 유지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp), // (20dp 마진) - (IconButton 내부 잉여 여백 12dp) = 8dp
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.receipt_detail_menu_close),
                        tint = Color(0xFF111827), // ColorGray900
                    )
                }
            }

            // 본문 콘텐츠 영역 (좌우 20dp 일괄 통제)
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // 스크린샷 텐션에 맞춰 아이콘 크기(48dp) 및 간격 최적화
                AsyncImage(
                    model = R.drawable.shiny_white,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.mypage_promo_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827), // ColorGray900
                    lineHeight = 32.sp, // 읽기 편한 행간
                )

                Spacer(Modifier.height(24.dp))

                // 이벤트 안내 회색 박스
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)) // Rounded2xl
                        .background(Color(0xFFF9FAFB)) // ColorGray50
                        .padding(20.dp), // 스크린샷 기준 넉넉한 내부 패딩
                ) {
                    Text(
                        text = stringResource(R.string.mypage_promo_notice_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF007AFF), // ColorBrandPrimary
                    )
                    Spacer(Modifier.height(12.dp))
                    PromoBulletText(stringResource(R.string.mypage_promo_notice_1))
                    Spacer(Modifier.height(8.dp))
                    PromoBulletText(stringResource(R.string.mypage_promo_notice_2))
                }

                Spacer(Modifier.height(32.dp))

                // CTA 버튼
                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp), // RoundedXl
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF), // ColorBrandPrimary
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.mypage_promo_register),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, // 스크린샷의 볼드 텐션 적용
                    )
                }
            }
        }
    }
}

/** * [교정 3] "•" 불릿 + 문구 (Hanging Indent 픽셀 보정)
 * 문구가 2~3줄로 줄바꿈되어도 불릿은 무조건 상단에 고정되도록 Alignment.Top 명시
 */
@Composable
private fun PromoBulletText(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "•",
            fontSize = 14.sp,
            color = Color(0xFF111827), // ColorGray900 (불릿은 더 진하게)
            modifier = Modifier.padding(end = 8.dp, top = 2.dp) // 시각적 중심 맞춤
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF374151), // ColorGray800
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f),
        )
    }
}