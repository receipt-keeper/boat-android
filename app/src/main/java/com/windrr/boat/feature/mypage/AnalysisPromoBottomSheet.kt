package com.windrr.boat.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R

/** \"영수증 분석\" 배너 \"보기\" 클릭 시 뜨는 무료 분석 이벤트 안내 BottomSheet. */
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
        containerColor = Color.White,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 24.dp)
                .navigationBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.common_close),
                        tint = Color(0xFF111827), // ColorGray900
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                Text(
                    text = "지금 바로 영수증을 쉽고\n빠르게 등록해 보세요!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827), // ColorGray900
                    lineHeight = 32.sp,
                )

                Spacer(Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.bobo_mypage),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                )

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF9FAFB)) // ColorGray50
                        .padding(20.dp),
                ) {
                    Text(
                        text = "오픈 이벤트 안내",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF007AFF), // ColorBrandPrimary
                    )
                    Spacer(Modifier.height(12.dp))
                    PromoBulletText(
                        buildAnnotatedString {
                            append("매월 1일 ")
                            withStyle(SpanStyle(color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)) {
                                append("무료 분석 5회")
                            }
                            append("가 지급되며, 매월 1일 초기화됩니다.")
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    PromoBulletText(
                        buildAnnotatedString {
                            append("가입 후 첫 달 이벤트로, ")
                            withStyle(SpanStyle(color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)) {
                                append("무료 분석 5회")
                            }
                            append("를 모두 사용할 시에 5회를 추가로 제공합니다. (계정당 1회)")
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    PromoBulletText(
                        buildAnnotatedString {
                            append("이벤트로 제공되는 ")
                            withStyle(SpanStyle(color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)) {
                                append("추가 5회")
                            }
                            append("는 지급된 달의 말일까지 사용이 가능하며, 사용하지 않은 횟수는 자동 소멸됩니다.")
                        }
                    )
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF), // ColorBrandPrimary
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = "지금 영수증 등록하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun PromoBulletText(text: CharSequence) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "•",
            fontSize = 14.sp,
            color = Color(0xFF111827),
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        )
        if (text is String) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color(0xFF374151), // ColorGray800
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f),
            )
        } else {
            Text(
                text = text as androidx.compose.ui.text.AnnotatedString,
                fontSize = 14.sp,
                color = Color(0xFF374151), // ColorGray800
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
