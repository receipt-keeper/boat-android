package com.windrr.boat.feature.receipt

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.windrr.boat.R
import com.windrr.boat.core.util.createImageCaptureUri
import com.windrr.boat.feature.gallery.GalleryIntent
import com.windrr.boat.feature.gallery.GalleryState
import com.windrr.boat.feature.gallery.GalleryViewModel
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.FreeAnalysisBanner
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandTertiary
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 영수증 기기 등록 화면 — 무료 분석 배너 + 업로드 영역(카메라/갤러리 다중선택, X 삭제) + 분석 시작.
 * 영수증을 1장 이상 올려야 "영수증 분석 시작하기"가 활성화된다.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReceiptRegisterScreen(
    freeAnalysisTokens: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    autoLaunch: String? = null,
    galleryViewModel: GalleryViewModel = viewModel(),
) {
    val galleryState by galleryViewModel.state.collectAsState()
    val photos = galleryState.photos
    val context = LocalContext.current
    val toastState = rememberBoatToastState()

    // 카메라 촬영 결과 저장 URI (촬영 직전 생성, 프로세스 죽음에도 보존)
    var cameraImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    // 무료 분석 토큰 소진 시 BottomSheet 표시 여부
    var showNoTokenSheet by rememberSaveable { mutableStateOf(false) }
    // 분석 실패 BottomSheet 표시 여부
    var showAnalysisFailedSheet by rememberSaveable { mutableStateOf(false) }

    // 최대 장수 초과 등 에러 → 토스트
    androidx.compose.runtime.LaunchedEffect(galleryState.error) {
        galleryState.error?.let {
            toastState.showError(it)
            galleryViewModel.handleIntent(GalleryIntent.ClearError)
        }
    }

    // 남은 등록 가능 장수 (이미 올린 만큼 제외)
    val remainingSlots = (GalleryState.MAX_PHOTOS - photos.size).coerceAtLeast(0)

    // 단일 선택 런처 (남은 슬롯이 1장일 때 — PickMultiple은 maxItems>=2만 허용)
    val singlePickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(listOf(uri)))
    }

    // 다중 선택 런처 — 남은 슬롯 수에 맞춰 재생성(key)해서 시스템 피커 선택 개수를 제한
    val multiPickLauncher = key(remainingSlots) {
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(remainingSlots.coerceAtLeast(2))
        ) { uris ->
            if (uris.isNotEmpty()) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(uris))
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        val uri = cameraImageUri
        if (isSuccess && uri != null) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(listOf(uri)))
    }

    fun launchCamera() {
        val uri = context.createImageCaptureUri()
        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (granted) launchCamera()
    }

    fun showMaxReached() {
        toastState.showError(context.getString(R.string.receipt_max_photos, GalleryState.MAX_PHOTOS))
    }

    fun onPickFromGallery() {
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        when {
            remainingSlots <= 0 -> showMaxReached()
            remainingSlots == 1 -> singlePickLauncher.launch(request)   // 1장만 선택
            else -> multiPickLauncher.launch(request)                   // 남은 만큼만 선택
        }
    }

    fun onTakePhoto() {
        if (remainingSlots <= 0) {
            showMaxReached()
            return
        }
        if (cameraPermission.status.isGranted) launchCamera()
        else cameraPermission.launchPermissionRequest()
    }

    // FAB 메뉴에서 진입한 경우 선택한 소스를 1회 자동 실행 (회전 후 재실행 방지)
    var autoLaunchHandled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!autoLaunchHandled) {
            autoLaunchHandled = true
            when (autoLaunch) {
                ReceiptRegisterActivity.LAUNCH_CAMERA -> onTakePhoto()
                ReceiptRegisterActivity.LAUNCH_GALLERY -> onPickFromGallery()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = ColorWhite,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = stringResource(R.string.common_back),
                                tint = Color.Unspecified,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorWhite),
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Margin20),
            ) {
                Spacer(Modifier.height(Margin8))
                FreeAnalysisBanner(remaining = freeAnalysisTokens)

                Spacer(Modifier.height(Margin24))
                Text(
                    text = stringResource(R.string.receipt_register_uploaded),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorGray900,
                )

                Spacer(Modifier.height(Margin16))
                // 업로드 영역 — 비어있으면 점선 placeholder, 있으면 3열 썸네일 그리드
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    if (photos.isEmpty()) {
                        UploadPlaceholder()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(photos) { uri ->
                                ReceiptThumbnail(
                                    uri = uri,
                                    onRemove = { galleryViewModel.handleIntent(GalleryIntent.RemovePhoto(uri)) },
                                )
                            }
                        }
                    }
                }

                OutlineActionButton(
                    icon = R.drawable.ic_camera,
                    label = R.string.receipt_register_camera,
                    onClick = { onTakePhoto() },
                )
                Spacer(Modifier.height(Margin12))
                OutlineActionButton(
                    icon = R.drawable.ic_gallery,
                    label = R.string.receipt_register_gallery,
                    onClick = { onPickFromGallery() },
                )

                Spacer(Modifier.height(Margin24))
                Button(
                    onClick = {
                        if (freeAnalysisTokens <= 0) {
                            showNoTokenSheet = true // 무료 분석 횟수 소진 → 안내 BottomSheet
                        } else {
                            // TODO: OCR 분석 API 호출 → 성공 시 결과 화면, 실패 시 실패 시트.
                            // 현재 API 미연동 → 실패 케이스 디자인 확인용으로 실패 시트 표시
                            showAnalysisFailedSheet = true
                        }
                    },
                    enabled = photos.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedXl,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorBrandPrimary,
                        contentColor = ColorWhite,
                        disabledContainerColor = ColorGray200,
                        disabledContentColor = ColorGray500,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.receipt_register_analyze),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(Margin16))
            }
        }

        BoatToastHost(state = toastState)
    }

    if (showNoTokenSheet) {
        NoTokenBottomSheet(
            onDismiss = { showNoTokenSheet = false },
            onRecharge = { showNoTokenSheet = false /* TODO: 무료 충전 */ },
            onManualInput = {
                showNoTokenSheet = false
                context.startActivity(ReceiptManualInputActivity.intent(context))
            },
        )
    }

    if (showAnalysisFailedSheet) {
        AnalysisFailedBottomSheet(
            onDismiss = { showAnalysisFailedSheet = false },
            onManualInput = {
                showAnalysisFailedSheet = false
                context.startActivity(ReceiptManualInputActivity.intent(context))
            },
            onRetry = { showAnalysisFailedSheet = false /* TODO: 다시 분석 시도 */ },
        )
    }
}

/** 업로드된 영수증 썸네일 + 우측 상단 X 삭제 버튼 */
@Composable
private fun ReceiptThumbnail(uri: Uri, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✕", color = ColorWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** 점선 테두리 업로드 placeholder (빈 상태) */
@Composable
private fun UploadPlaceholder() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .drawBehind {
                drawRoundRect(
                    color = ColorGray300,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_gallery),
            contentDescription = null,
            tint = ColorGray400,
            modifier = Modifier.size(32.dp),
        )
    }
}

/** 카메라/갤러리 외곽선 버튼 (연한 brand 보더 + brand 아이콘/텍스트) */
@Composable
private fun OutlineActionButton(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedXl)
            .border(1.dp, ColorBrandTertiary, RoundedXl)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = ColorBrandPrimary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Margin8))
        Text(
            text = stringResource(label),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = ColorBrandPrimary,
        )
    }
}
