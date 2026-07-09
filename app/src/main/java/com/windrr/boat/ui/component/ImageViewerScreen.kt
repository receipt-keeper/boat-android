package com.windrr.boat.ui.component

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.windrr.boat.data.remote.model.ReceiptFile
import com.windrr.boat.data.remote.ApiClient

/**
 * 이미지 뷰어 화면 — 카카오톡 스타일의 전체 화면 이미지 뷰어
 * 
 * @param images 이미지 URI 리스트 (갤러리에서 선택한 사진 등)
 * @param receiptFiles 서버에서 받은 영수증 파일 리스트
 * @param initialIndex 초기에 보여줄 이미지 인덱스
 * @param onClose 뷰어 닫기 콜백
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewerScreen(
    images: List<Uri> = emptyList(),
    receiptFiles: List<ReceiptFile> = emptyList(),
    initialIndex: Int = 0,
    onClose: () -> Unit,
) {
    val allImages = if (images.isNotEmpty()) {
        images.map { it.toString() }
    } else {
        receiptFiles.map {
            "${ApiClient.BASE_URL_PROD}${it.contentPath.trimStart('/')}"
        }
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

    // 상단 바 표시 여부 (탭하여 토글)
    var showTopBar by rememberSaveable { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showTopBar = !showTopBar }
                )
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = allImages[page],
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 반투명 상단 메뉴 바
        if (showTopBar) {
            ImageViewerTopBar(
                currentIndex = currentPageIndex,
                totalCount = allImages.size,
                onClose = onClose,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * 이미지 뷰어 상단 바 — 반투명 배경 + 닫기 버튼 + 이미지 카운터
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
            .height(56.dp)
            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 닫기 버튼 (좌측)
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        // 이미지 카운터 (우측) - 예: "1/5"
        Text(
            text = "$currentIndex/$totalCount",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
