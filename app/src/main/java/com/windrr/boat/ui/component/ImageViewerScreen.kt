package com.windrr.boat.ui.component

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.model.ReceiptFile

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewerScreen(
    images: List<Uri> = emptyList(),
    receiptFiles: List<ReceiptFile> = emptyList(),
    /** 로컬/원격이 섞인 경우 Coil model(Any: Uri, URL String 등)을 순서대로 직접 넘긴다. 지정 시 우선한다. */
    models: List<Any> = emptyList(),
    initialIndex: Int = 0,
    onClose: () -> Unit,
) {
    val allImages: List<Any> = when {
        models.isNotEmpty() -> models
        images.isNotEmpty() -> images.map { it.toString() }
        else -> receiptFiles.map { "${ApiClient.BASE_URL_PROD}${it.contentPath.trimStart('/')}" }
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { allImages.size }
    )

    LaunchedEffect(initialIndex) {
        if (initialIndex in allImages.indices) {
            pagerState.scrollToPage(initialIndex)
        }
    }

    // 현재 페이지 인덱스 (1-based)
    val currentPageIndex by remember {
        derivedStateOf { pagerState.currentPage + 1 }
    }

    // 상단 바 표시 여부
    var showTopBar by rememberSaveable { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // 💡 [추가] 확대/축소를 지원하는 커스텀 이미지 뷰어 컴포넌트 적용
            ZoomableImage(
                model = allImages[page],
                onToggleTopBar = { showTopBar = !showTopBar }
            )
        }

        // 💡 [수정] 스크린샷 가이드에 맞춘 그라데이션 오버레이 상단 바
        if (showTopBar) {
            ImageViewerTopBar(
                currentIndex = currentPageIndex,
                totalCount = allImages.size,
                onClose = onClose,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

/**
 * 💡 [추가] 핀치 줌(확대/축소), 패닝(이동), 더블 탭 원복을 지원하는 이미지 컴포넌트
 * HorizontalPager의 좌우 스와이프와 충돌하지 않도록 배율(scale)이 1.0 초과일 때만 제스처를 소비합니다.
 */
@Composable
private fun ZoomableImage(
    model: Any,
    onToggleTopBar: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // 더블 탭 시 기본 크기 및 중앙 위치로 리셋
                        scale = 1f
                        offset = Offset.Zero
                    },
                    onTap = { onToggleTopBar() }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()

                        // 1배 ~ 4배까지 확대 허용
                        scale = (scale * zoom).coerceIn(1f, 4f)

                        if (scale > 1f) {
                            // 확대 상태일 때는 이동(Pan) 바운더리 계산 및 제스처 소비(Consume)
                            val maxX = (size.width * (scale - 1)) / 2
                            val maxY = (size.height * (scale - 1)) / 2
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                            )
                            // 💡 Pager로 제스처가 넘어가지 않도록 이벤트를 소비합니다.
                            event.changes.forEach { it.consume() }
                        } else {
                            // 1배율일 때는 오프셋 초기화 및 제스처 소비 안함 -> Pager가 스와이프를 가져감
                            offset = Offset.Zero
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}

/**
 * 💡 [수정] 이미지 뷰어 상단 바
 * - 둥근 박스 형태에서, 상단 엣지부터 부드럽게 떨어지는 그라데이션 오버레이로 변경 (가독성 확보)
 * - 좌우 패딩을 주어 닫기 버튼과 카운터 텍스트를 양 끝에 배치
 */
@Composable
private fun ImageViewerTopBar(
    currentIndex: Int,
    totalCount: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding() // 상태표시줄(노치) 영역 침범 방지
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 16.dp), // 텍스트 아래로 그라데이션이 충분히 깔리도록 하단 여백 추가
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 닫기 버튼 (좌측 정렬)
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        // 이미지 카운터 (우측 정렬) - 디자인 가이드에 맞춰 폰트 크기 및 자간(letterSpacing) 조정
        Text(
            text = "$currentIndex / $totalCount",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp
        )
    }
}