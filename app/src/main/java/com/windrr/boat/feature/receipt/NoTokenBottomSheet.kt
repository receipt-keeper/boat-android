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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
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
 * 안내 박스 + "5회 무료로 충전하기"/"영수증 직접 입력하기" 2버튼 노출.
 * [canRecharge] false — 이미 이번 달 수령했거나 노출할 프로모션이 없을 때. 중앙 정렬
 * 레이아웃으로 "영수증 직접 입력하기" 단일 버튼만 노출.
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

    // 디자인 토큰 색상 매핑 (프로젝트 환경에 맞춰 치환)
    val colorWhite = Color.White
    val colorGray900 = Color(0xFF111827)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorWhite,
        dragHandle = null, // 상단 기본 핸들 바 제거 (디자인 가이드 반영)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            // 우측 상단 닫기(X) 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 12.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "닫기",
                        tint = colorGray900,
                    )
                }
            }

            // 분기별 콘텐츠 렌더링
            if (canRecharge) {
                NoTokenPromoContent(onRecharge = onRecharge, onManualInput = onManualInput)
            } else {
                NoTokenSimpleContent(onManualInput = onManualInput)
            }
        }
    }
}

/** [canRecharge = true] 충전 프로모션 O: 좌측 정렬 + 유의사항 박스 + 2버튼 */
@Composable
private fun NoTokenPromoContent(
    onRecharge: () -> Unit,
    onManualInput: () -> Unit,
) {
    val colorBrandPrimary = Color(0xFF007AFF) // 시그니처 블루
    val colorGray900 = Color(0xFF111827)
    val colorGray600 = Color(0xFF4B5563)
    val colorGrayBg = Color(0xFFF3F4F6) // 안내 박스 옅은 회색
    val roundedXl = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // 아이콘 (좌측 정렬)
        AsyncImage(
            model = R.drawable.shiny_white, // 반짝이는 AI 아이콘 리소스
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )

        Spacer(Modifier.height(16.dp))

        // 타이틀
        Text(
            text = "영수증 분석 횟수를 다 쓰셨네요!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorGray900,
        )
        Spacer(Modifier.height(8.dp))

        // 서브타이틀 (블루, "무료 분석 5회"만 강조)
        Text(
            text = buildAnnotatedString {
                append("오픈 이벤트로 ")
                withStyle(SpanStyle(color = colorBrandPrimary)) {
                    append("무료 분석 5회")
                }
                append("를 추가로 드려요.")
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorBrandPrimary,
        )

        Spacer(Modifier.height(24.dp))

        // 유의사항 안내 박스
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(roundedXl)
                .background(colorGrayBg)
                .padding(20.dp),
        ) {
            Text(
                text = "확인해 주세요",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colorGray900,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "•",
                    fontSize = 14.sp,
                    color = colorGray900,
                    modifier = Modifier.padding(end = 8.dp, top = 1.dp),
                )
                Text(
                    text = buildAnnotatedString {
                        append("이벤트로 제공되는 ")
                        withStyle(
                            SpanStyle(
                                color = colorBrandPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("추가 5회")
                        }
                        append("는 지급된 달의 말일까지 사용할 수 있으며, 사용하지 않은 횟수는 자동으로 소멸됩니다. (계정당 1회)")
                    },
                    fontSize = 14.sp,
                    color = colorGray600,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Primary CTA Button (충전하기)
        Button(
            onClick = onRecharge,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = roundedXl,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorBrandPrimary,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = "5회 무료로 충전하기",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(12.dp))

        // Secondary CTA Button (직접 입력하기)
        OutlinedButton(
            onClick = onManualInput,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = roundedXl,
            border = BorderStroke(1.dp, colorBrandPrimary.copy(alpha = 0.3f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colorBrandPrimary),
        ) {
            Text(
                text = "영수증 직접 입력하기",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** [canRecharge = false] 충전 프로모션 X: 중앙 정렬 + 직접입력 단일 버튼 */
@Composable
private fun NoTokenSimpleContent(onManualInput: () -> Unit) {
    val colorBrandPrimary = Color(0xFF007AFF)
    val colorGray900 = Color(0xFF111827)
    val colorGray500 = Color(0xFF6B7280)
    val roundedXl = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 아이콘 (중앙 정렬)
        AsyncImage(
            model = R.drawable.shiny_white,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )

        Spacer(Modifier.height(16.dp))

        // 타이틀 (중앙 정렬)
        Text(
            text = "영수증 분석 횟수를 다 쓰셨네요!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorGray900,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))

        // 서브타이틀 (그레이 & 레귤러 & 중앙 정렬)
        Text(
            text = "영수증을 직접 입력하면 계속\n보증 기간을 관리할 수 있어요.",
            fontSize = 15.sp,
            color = colorGray500,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(32.dp))

        // 단일 CTA Button (직접 입력하기)
        Button(
            onClick = onManualInput,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = roundedXl,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorBrandPrimary,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = "영수증 직접 입력하기",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}