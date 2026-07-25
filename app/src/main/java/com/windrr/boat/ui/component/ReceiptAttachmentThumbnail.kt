package com.windrr.boat.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.windrr.boat.R
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.model.ReceiptFile
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.PretendardFontFamily
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 첨부 파일(contentPath) → 절대 URL. 인증 헤더는 전역 Coil ImageLoader가 자동으로 붙인다(AppCore 참고).
 * 등록/상세/수정 화면과 이미지 뷰어가 동일한 규칙으로 URL을 만들도록 공용화한다.
 */
fun ReceiptFile.toContentUrl(): String =
    "${ApiClient.BASE_URL_PROD}${contentPath.trimStart('/')}"

/**
 * 영수증 첨부 이미지 썸네일 (등록/상세/수정 화면 공용).
 * - [model]: Coil이 로드할 대상 (로컬 Uri 또는 원격 URL String 등)
 * - [onClick]: 탭 시 동작 — 보통 ImageViewerScreen 오픈
 * - [onRemove]: null이면 X 삭제 버튼 미표시(읽기 전용). 값이 있으면 우상단 X 버튼 노출
 * - [showError]: 분석 실패 등 오류 오버레이 표시
 *
 * 크기는 호출부의 [modifier](예: `Modifier.size(100.dp)`)로 제어한다.
 */
@Composable
fun ReceiptAttachmentThumbnail(
    model: Any?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onRemove: (() -> Unit)? = null,
    showError: Boolean = false,
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedXl),
        )

        if (showError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedXl)
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.5.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.error),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        text = "다시 업로드해 주세요",
                        color = Color(0xFFFE395B),
                        // caption3 Bold — Pretendard 700 / 10sp / 행간 130%(13sp) / 자간 0
                        style = TextStyle(
                            fontFamily = PretendardFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            letterSpacing = 0.sp,
                            // 폰트 기본 상하 패딩 제거 + 행간을 텍스트 중앙에 배치해,
                            // 아이콘+텍스트 묶음이 실제 시각적 중앙에 오도록 한다(위쪽 쏠림 방지).
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both,
                            ),
                        ),
                    )
                }
            }
        }

        if (onRemove != null) {
            // 시각적 크기(24.dp)는 그대로 두고, 탭 영역만 사방 2dp씩 넓힌 바깥 Box에 clickable을 건다.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(28.dp)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    // 텍스트 글리프("✕")는 베이스라인 때문에 원 안에서 살짝 치우쳐 보여
                    // 벡터 아이콘으로 교체해 정확히 중앙 정렬한다.
                    Icon(
                        painter = painterResource(R.drawable.icon_close),
                        contentDescription = null,
                        tint = ColorWhite,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}
