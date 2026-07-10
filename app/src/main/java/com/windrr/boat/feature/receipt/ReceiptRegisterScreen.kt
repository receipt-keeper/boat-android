package com.windrr.boat.feature.receipt

import android.Manifest
import android.content.Intent
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.windrr.boat.R
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.core.util.createImageCaptureUri
import com.windrr.boat.core.util.toMultipartPart
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.model.PromotionState
import com.windrr.boat.data.repository.PromotionRepository
import com.windrr.boat.feature.gallery.GalleryIntent
import com.windrr.boat.feature.gallery.GalleryState
import com.windrr.boat.feature.gallery.GalleryViewModel
import com.windrr.boat.feature.notification.NotificationBadgeViewModel
import com.windrr.boat.feature.notification.NotificationListActivity
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.ImageViewerScreen
import com.windrr.boat.ui.component.ReceiptAttachmentThumbnail
import com.windrr.boat.ui.component.SyncLoadingOverlay
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorBrandTertiary
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray700
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Rounded2xl
import com.windrr.boat.ui.theme.RoundedFull
import com.windrr.boat.ui.theme.RoundedXl
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

private val ThumbnailSize = 130.dp

/**
 * 영수증 기기 등록 화면 — 카메라/갤러리 업로드 카드 + 유의사항 + 첨부내역(가로 스크롤) + 분석 시작.
 * 영수증을 1장 이상 올려야 "영수증 분석 시작하기"가 활성화된다.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReceiptRegisterScreen(
    freeAnalysisTokens: Int,
    remoteCanAnalyze: Boolean?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    autoLaunch: String? = null,
    onUsageChanged: () -> Unit = {},
    galleryViewModel: GalleryViewModel = viewModel(),
    badgeViewModel: NotificationBadgeViewModel = viewModel(),
) {
    val galleryState by galleryViewModel.state.collectAsState()
    val photos = galleryState.photos
    val context = LocalContext.current
    val toastState = rememberBoatToastState()
    val scope = rememberCoroutineScope()
    val hasUnreadNotification by badgeViewModel.hasUnread.collectAsState()

    // 카메라 촬영 결과 저장 URI (촬영 직전 생성, 프로세스 죽음에도 보존)
    var cameraImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    // 무료 분석 토큰 소진 시 BottomSheet 표시 여부
    var showNoTokenSheet by rememberSaveable { mutableStateOf(false) }
    // 분석 실패 BottomSheet 표시 여부
    var showAnalysisFailedSheet by rememberSaveable { mutableStateOf(false) }
    // OCR 분석 진행 중 여부 (버튼 로딩/중복 호출 방지)
    var isAnalyzing by rememberSaveable { mutableStateOf(false) }
    // OCR 분석 실패 여부 — 실패 시 각 썸네일에 에러 오버레이 표시
    var isAnalysisFailed by rememberSaveable { mutableStateOf(false) }
    // 월간 크레딧 충전 진행 중 여부 (버튼 중복 클릭 방지 + 로딩 오버레이)
    var isRecharging by rememberSaveable { mutableStateOf(false) }
    // 이번 달 충전 프로모션 수령 가능 여부 (조회 전 null → 조회 완료 시 결정). 시트 충전 버튼 노출 제어.
    var canRecharge by rememberSaveable { mutableStateOf(true) }
    // 이미지 뷰어 표시 여부
    var showImageViewer by rememberSaveable { mutableStateOf(false) }
    var initialImageIndex by rememberSaveable { mutableStateOf(0) }

    val promotionRepository = remember { PromotionRepository() }
    // 프로모션 수령 멱등키 — 재시도 시 동일 값을 재사용해 중복 수령을 방지한다.
    val rechargeIdempotencyKey = rememberSaveable { UUID.randomUUID().toString() }
    val rechargeFailedMessage = stringResource(R.string.token_empty_recharge_failed)
    val alreadyRedeemedMessage = stringResource(R.string.token_recharge_already)
    val unavailableMessage = stringResource(R.string.token_recharge_unavailable)

    LaunchedEffect(Unit) { badgeViewModel.refresh() }

    /**
     * 월간 OCR 충전 프로모션 수령 흐름.
     * 프로모션 조회 → state=redeemable이면 수령 → balance.remainingCount를 잔여 크레딧에 반영 후 usage 재조회.
     * 이미 수령/노출 없음 등은 상태별 안내 토스트를 띄운다.
     */
    fun rechargeOcrCredits() {
        if (isRecharging) return
        scope.launch {
            isRecharging = true
            promotionRepository.getOcrRechargePromotion().fold(
                onSuccess = { promo ->
                    val promotionId = promo.promotionId
                    if (promo.stateType == PromotionState.REDEEMABLE && promotionId != null) {
                        promotionRepository.redeem(promotionId, rechargeIdempotencyKey).fold(
                            onSuccess = { redeemed ->
                                isRecharging = false
                                showNoTokenSheet = false
                                redeemed.balance?.remainingCount?.let {
                                    ApiClient.userDataStore.updateFreeAnalysisTokens(it)
                                }
                                onUsageChanged()
                            },
                            onFailure = { e ->
                                isRecharging = false
                                BoatLog.e("OCR 충전 수령 실패", e)
                                // 409 등 이미 수령 상태면 충전 버튼을 숨기고 안내
                                canRecharge = false
                                toastState.showError(alreadyRedeemedMessage)
                            },
                        )
                    } else {
                        // redeemable이 아니면 수령 불가 — 충전 버튼 숨김 + 상태별 안내
                        isRecharging = false
                        canRecharge = false
                        val message = when (promo.stateType) {
                            PromotionState.ALREADY_REDEEMED -> alreadyRedeemedMessage
                            else -> unavailableMessage
                        }
                        toastState.showError(message)
                    }
                },
                onFailure = { e ->
                    isRecharging = false
                    BoatLog.e("OCR 충전 프로모션 조회 실패", e)
                    toastState.showError(rechargeFailedMessage)
                },
            )
        }
    }

    /** 토큰 소진 시트를 열기 전, 이번 달 충전 프로모션 수령 가능 여부를 조회해 충전 버튼 노출을 결정한다. */
    fun openNoTokenSheet() {
        showNoTokenSheet = true
        scope.launch {
            promotionRepository.getOcrRechargePromotion().fold(
                onSuccess = { canRecharge = it.stateType == PromotionState.REDEEMABLE },
                onFailure = { canRecharge = false },
            )
        }
    }

    // 영수증 OCR 분석 호출 → 성공 시 결과 화면, 실패 시 실패 시트
    fun analyzeReceipt() {
        if (isAnalyzing) return
        scope.launch {
            isAnalyzing = true
            isAnalysisFailed = false
            runCatching {
                // 여러 장을 병렬로 읽어 업로드 시작까지의 지연(=토큰 만료 위험)을 최소화
                val parts = coroutineScope {
                    photos.map { uri -> async { uri.toMultipartPart(context, "file") } }
                        .awaitAll()
                }
                ApiClient.ocrApiService.analyze(parts)
            }.onSuccess { response ->
                isAnalyzing = false
                context.startActivity(
                    ReceiptManualInputActivity.intent(context, photos, response.data)
                )
                scope.launch {
                    val newCount = (freeAnalysisTokens - 1).coerceAtLeast(0)
                    ApiClient.userDataStore.updateFreeAnalysisTokens(newCount)
                }
            }.onFailure { e ->
                isAnalyzing = false
                isAnalysisFailed = true
                BoatLog.e("OCR 분석 실패", e)
                showAnalysisFailedSheet = true
            }
        }
    }

    // 최대 장수 초과 등 에러 → 토스트
    LaunchedEffect(galleryState.error) {
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
        if (isSuccess && uri != null) galleryViewModel.handleIntent(
            GalleryIntent.AddPhotos(
                listOf(
                    uri
                )
            )
        )
    }

    fun launchCamera() {
        val uri = context.createImageCaptureUri()
        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (granted) launchCamera()
    }

    fun showMaxReached(errorMessage: String) {
        toastState.showError(errorMessage)
    }

    fun onPickFromGallery(errorMessage: String) {
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        when {
            remainingSlots <= 0 -> showMaxReached(errorMessage)
            remainingSlots == 1 -> singlePickLauncher.launch(request)   // 1장만 선택
            else -> multiPickLauncher.launch(request)                   // 남은 만큼만 선택
        }
    }

    fun onTakePhoto(errorMessage: String) {
        if (remainingSlots <= 0) {
            showMaxReached(errorMessage)
            return
        }
        if (cameraPermission.status.isGranted) launchCamera()
        else cameraPermission.launchPermissionRequest()
    }

    // FAB 메뉴에서 진입한 경우 선택한 소스를 1회 자동 실행 (회전 후 재실행 방지)
    var autoLaunchHandled by rememberSaveable { mutableStateOf(false) }
    val maxPhotosErrorMessage = stringResource(R.string.receipt_max_photos, GalleryState.MAX_PHOTOS)
    LaunchedEffect(Unit) {
        if (!autoLaunchHandled) {
            autoLaunchHandled = true
            when (autoLaunch) {
                ReceiptRegisterActivity.LAUNCH_CAMERA -> onTakePhoto(maxPhotosErrorMessage)
                ReceiptRegisterActivity.LAUNCH_GALLERY -> onPickFromGallery(maxPhotosErrorMessage)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = ColorWhite,
            topBar = {
                BoatHeader(
                    modifier = Modifier.statusBarsPadding(), // 시스템 상태바와 겹치지 않도록 inset 확보
                    hasUnreadNotification = hasUnreadNotification,
                    onBackClick = onBack,
                    onSearchClick = { context.startActivity(SearchActivity.intent(context)) },
                    onNotificationClick = {
                        context.startActivity(Intent(context, NotificationListActivity::class.java))
                    },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Column(modifier = Modifier.padding(horizontal = Margin20)) {
                        Spacer(Modifier.height(Margin8))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.receipt_register_title),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorGray900,
                                modifier = Modifier.weight(1f),
                            )
                            AnalysisCountPill(remaining = freeAnalysisTokens)
                        }

                        Spacer(Modifier.height(Margin20))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            UploadActionCard(
                                icon = R.drawable.ic_camera,
                                label = R.string.receipt_register_camera,
                                onClick = { onTakePhoto(maxPhotosErrorMessage) },
                                modifier = Modifier.weight(1f),
                            )
                            UploadActionCard(
                                icon = R.drawable.ic_gallery,
                                label = R.string.receipt_register_gallery,
                                onClick = { onPickFromGallery(maxPhotosErrorMessage) },
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Spacer(Modifier.height(Margin16))
                        NoticeCard()

                        if (photos.isNotEmpty()) {
                            Spacer(Modifier.height(Margin24))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.receipt_register_attachments),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorGray900,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "${photos.size}/${GalleryState.MAX_PHOTOS}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorBrandPrimary,
                                )
                            }
                            Spacer(Modifier.height(Margin12))
                        }
                    }

                    if (photos.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = Margin20),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(photos) { uri ->
                                val index = photos.indexOf(uri)
                                ReceiptAttachmentThumbnail(
                                    model = uri,
                                    showError = isAnalysisFailed,
                                    onRemove = {
                                        galleryViewModel.handleIntent(GalleryIntent.RemovePhoto(uri))
                                    },
                                    onClick = {
                                        initialImageIndex = index
                                        showImageViewer = true
                                    },
                                    modifier = Modifier.size(ThumbnailSize),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Margin20))
                }

                Column(modifier = Modifier.padding(horizontal = Margin20)) {
                    val networkErrorMessage = stringResource(R.string.receipt_check_network)
                    Button(
                        onClick = {
                            when {
                                remoteCanAnalyze == null -> toastState.showError(networkErrorMessage)
                                remoteCanAnalyze && freeAnalysisTokens > 0 -> analyzeReceipt()
                                else -> openNoTokenSheet()
                            }
                        },
                        enabled = photos.isNotEmpty() && !isAnalyzing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedXl,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorBrandPrimary,
                            contentColor = ColorWhite,
                            disabledContainerColor = ColorGray200,
                            disabledContentColor = ColorGray400,
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
        }

        BoatToastHost(state = toastState)
        if (isAnalyzing) {
            SyncLoadingOverlay(message = stringResource(R.string.loading_ocr_message))
        }
        if (isRecharging) {
            SyncLoadingOverlay(message = stringResource(R.string.loading_recharge_message))
        }
    }

    if (showNoTokenSheet) {
        NoTokenBottomSheet(
            onDismiss = { showNoTokenSheet = false },
            onRecharge = { rechargeOcrCredits() },
            onManualInput = {
                showNoTokenSheet = false
                context.startActivity(ReceiptManualInputActivity.intent(context, photos))
            },
            canRecharge = canRecharge,
        )
    }

    if (showAnalysisFailedSheet) {
        AnalysisFailedBottomSheet(
            onDismiss = { showAnalysisFailedSheet = false },
            onManualInput = {
                showAnalysisFailedSheet = false
                context.startActivity(ReceiptManualInputActivity.intent(context, photos))
            },
            onRetry = {
                showAnalysisFailedSheet = false
                analyzeReceipt()
            },
        )
    }

    // ── 이미지 뷰어 ──
    if (showImageViewer) {
        ImageViewerScreen(
            images = photos,
            initialIndex = initialImageIndex,
            onClose = { showImageViewer = false }
        )
    }
}

/** "✦ 분석횟수 N회" pill — 타이틀 옆에 표시하는 잔여 분석 횟수 뱃지. */
@Composable
private fun AnalysisCountPill(remaining: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedFull)
            .background(ColorBrandSenary)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ai_color),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.receipt_register_analysis_count, remaining),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorBrandPrimary,
        )
    }
}

/** 카메라/갤러리 정사각 업로드 카드 — 흰 배경 + 연한 brand 테두리 + 큰 아이콘/라벨 */
@Composable
private fun UploadActionCard(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(Rounded2xl)
            .background(ColorWhite)
            .border(1.dp, ColorBrandTertiary, Rounded2xl)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = ColorBrandPrimary,
            modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(label),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorBrandPrimary,
        )
    }
}

/** "유의사항" 접기/펼치기 카드 — 헤더(정보 아이콘+타이틀+chevron)와 펼침 시 불릿 3개. */
@Composable
private fun NoticeCard() {
    // 최초 진입 시 기본으로 펼쳐진 상태
    var expanded by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedXl)
            .background(ColorWhite)
            .border(1.dp, ColorGray200, RoundedXl)
            .padding(Margin16),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { expanded = !expanded }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.toast_info),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.receipt_register_notice_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ColorGray900,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = ColorGray400,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(if (expanded) 270f else 90f),
            )
        }

        if (expanded) {
            Spacer(Modifier.height(Margin16))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                NoticeBulletRow(
                    icon = R.drawable.icon_images_upload,
                    prefix = "영수증은 ",
                    highlight = "제품 1개당 최대 5장까지",
                    suffix = " 업로드할 수 있습니다.",
                )
                NoticeBulletRow(
                    icon = R.drawable.ic_upload_single,
                    prefix = "영수증은 ",
                    highlight = "1장씩 개별 촬영하여",
                    suffix = " 업로드해 주세요.",
                )
                NoticeBulletRow(
                    icon = R.drawable.ic_file_format,
                    prefix = "",
                    highlight = "JPG, PNG, HEIC",
                    suffix = "만 등록해 주세요.",
                )
            }
        }
    }
}

/** 유의사항 불릿 한 줄 — 좌측 아이콘 + prefix/highlight(파랑)/suffix 텍스트. */
@Composable
private fun NoticeBulletRow(
    @DrawableRes icon: Int,
    prefix: String,
    highlight: String,
    suffix: String,
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = ColorGray600,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = buildAnnotatedString {
                append(prefix)
                withStyle(SpanStyle(color = ColorBrandPrimary, fontWeight = FontWeight.SemiBold)) {
                    append(highlight)
                }
                append(suffix)
            },
            fontSize = 13.sp,
            color = ColorGray700,
            lineHeight = 19.sp,
        )
    }
}

