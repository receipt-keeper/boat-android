package com.windrr.boat.feature.receipt

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.R
import com.windrr.boat.core.ocr.DeviceImage
import com.windrr.boat.core.util.toPriceString
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.ui.component.BoatDialog
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.ImageViewerScreen
import com.windrr.boat.ui.component.InfoTooltipIcon
import com.windrr.boat.ui.component.ReceiptAttachmentThumbnail
import com.windrr.boat.ui.component.SyncLoadingOverlay
import com.windrr.boat.ui.component.rememberShimmerBrush
import com.windrr.boat.ui.component.shimmer
import com.windrr.boat.ui.component.toContentUrl
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandQuinary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorGray100
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorSystemError
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Rounded2xl
import com.windrr.boat.ui.theme.RoundedLg
import com.windrr.boat.ui.theme.RoundedXl
import kotlinx.coroutines.delay

/** "2024-04-12" → "2024.04.12" */
private fun String?.toDotDate(): String {
    if (this.isNullOrBlank()) return "-"
    val parts = split("-")
    return if (parts.size == 3) "${parts[0]}.${parts[1]}.${parts[2]}" else this
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    receiptId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    // 등록완료 화면의 "보러가기"처럼 백스택 없이 바로 진입한 경우 true — 뒤로가기 대신 닫기(X) 아이콘을 보여준다.
    showCloseIcon: Boolean = false,
    viewModel: ReceiptDetailViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val toastState = rememberBoatToastState()
    var showMenuSheet by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var showImageViewer by rememberSaveable { mutableStateOf(false) }
    var initialImageIndex by rememberSaveable { mutableStateOf(0) }
    val deletedMessage = stringResource(R.string.receipt_deleted_toast)
    val deleteFailedMessage = stringResource(R.string.receipt_delete_failed)

    LaunchedEffect(receiptId) { viewModel.load(receiptId) }

    // 수정 화면(별도 Activity)에서 저장 성공(RESULT_OK) 후 복귀하면 변경 내용(새 사진 포함)을 재조회한다.
    // resume 타이밍에 의존하는 방식은 복귀 시점에 옛 상태가 그대로 남는 경우가 있어, 결과 기반으로 명시적으로 갱신한다.
    val editLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.load(receiptId)
        }
    }

    // 삭제 성공 — 토스트 표시 후 잠시 뒤 메인 화면(목록 탭)으로 강제 이동
    // (검색 결과에서 진입한 경우 등 검색 페이지로 돌아가면 삭제 결과가 반영되지 않은 상태일 수 있으므로 메인을 새로 띄운다)
    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            toastState.show(deletedMessage)
            delay(1000)
            context.startActivity(
                Intent(context, com.windrr.boat.feature.home.HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            (context as? Activity)?.finish()
        }
    }
    LaunchedEffect(state.deleteError) {
        if (state.deleteError != null) {
            toastState.showError(state.deleteError ?: deleteFailedMessage)
            viewModel.consumeDeleteError()
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
                                painter = painterResource(
                                    if (showCloseIcon) R.drawable.icon_close else R.drawable.ic_arrow_back
                                ),
                                contentDescription = stringResource(
                                    if (showCloseIcon) R.string.common_close else R.string.common_back
                                ),
                                tint = Color.Unspecified,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showMenuSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.common_more),
                                tint = ColorGray900,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorWhite),
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    // 이미 조회된 내용이 있으면(복귀 후 재조회 포함) 스피너 없이 기존 내용을 유지한 채 갱신
                    state.receipt != null -> ReceiptDetailContent(
                        receipt = state.receipt!!,
                        onImageClick = { index ->
                            initialImageIndex = index
                            showImageViewer = true
                        }
                    )

                    state.isLoading -> ReceiptDetailSkeleton()

                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error
                                    ?: stringResource(R.string.receipt_detail_load_failed),
                                fontSize = 14.sp,
                                color = ColorGray500,
                            )
                            TextButton(onClick = { viewModel.load(receiptId) }) {
                                Text(
                                    stringResource(R.string.receipt_list_retry),
                                    color = ColorBrandPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        BoatToastHost(state = toastState)
        if (state.isDeleting) {
            SyncLoadingOverlay(message = stringResource(R.string.loading_delete_message))
        }
    }

    // ── 케밥 메뉴 (수정하기 / 삭제하기 / 닫기) ──
    if (showMenuSheet) {
        ReceiptDetailMenuSheet(
            onDismiss = { showMenuSheet = false },
            onEdit = {
                showMenuSheet = false
                editLauncher.launch(ReceiptEditActivity.intent(context, receiptId))
            },
            onDelete = {
                showMenuSheet = false
                showDeleteConfirm = true
            },
        )
    }

    // ── 삭제 확인 다이얼로그 ──
    if (showDeleteConfirm) {
        BoatDialog(
            title = stringResource(R.string.receipt_delete_confirm_title),
            message = stringResource(R.string.receipt_delete_confirm_message),
            confirmText = stringResource(R.string.receipt_delete),
            confirmTextColor = ColorSystemError,
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = {
                showDeleteConfirm = false
                viewModel.delete(receiptId)
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }

    // ── 이미지 뷰어 ──
    if (showImageViewer && state.receipt != null) {
        ImageViewerScreen(
            receiptFiles = state.receipt!!.receiptFiles,
            initialIndex = initialImageIndex,
            onClose = { showImageViewer = false }
        )
    }
}

/** 케밥 클릭 시 뜨는 액션 바텀시트 — 수정하기/삭제하기 카드 + 별도 닫기 버튼 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptDetailMenuSheet(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20)
                .padding(bottom = Margin20),
            verticalArrangement = Arrangement.spacedBy(Margin8),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Rounded2xl)
                    .background(ColorWhite),
            ) {
                MenuSheetRow(
                    text = stringResource(R.string.receipt_detail_menu_edit),
                    color = ColorBrandPrimary,
                    onClick = onEdit,
                )
                HorizontalDivider(color = ColorGray100)
                MenuSheetRow(
                    text = stringResource(R.string.receipt_delete),
                    color = ColorSystemError,
                    onClick = onDelete,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Rounded2xl)
                    .background(ColorWhite)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.receipt_detail_menu_close),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorGray900,
                )
            }
        }
    }
}

@Composable
private fun MenuSheetRow(text: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

/** API 로딩 중 표시되는 상세 화면 스켈레톤 — 실제 콘텐츠 레이아웃(히어로/필드/섹션)을 셔머로 흉내낸다. */
@Composable
private fun ReceiptDetailSkeleton() {
    val brush = rememberShimmerBrush()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // 대표 이미지 히어로 카드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20)
                .padding(top = Margin8)
                .aspectRatio(16f / 9f)
                .shimmer(brush, Rounded2xl),
        )

        Spacer(Modifier.height(Margin24))
        Column(modifier = Modifier.padding(horizontal = Margin20)) {
            // 필드 3개 (라벨 바 + 값 바)
            repeat(3) {
                Box(Modifier.width(96.dp).height(13.dp).shimmer(brush))
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth(0.62f).height(18.dp).shimmer(brush))
                Spacer(Modifier.height(Margin20))
            }
            // 메모 라벨 + 박스
            Box(Modifier.width(60.dp).height(13.dp).shimmer(brush))
            Spacer(Modifier.height(Margin8))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .shimmer(brush, RoundedLg),
            )
        }

        // 실물 영수증 보관 여부 섹션
        SectionBand()
        Column(modifier = Modifier.padding(horizontal = Margin20, vertical = Margin20)) {
            Box(Modifier.width(140.dp).height(18.dp).shimmer(brush))
            Spacer(Modifier.height(Margin16))
            Box(Modifier.width(110.dp).height(14.dp).shimmer(brush))
            Spacer(Modifier.height(Margin12))
            Box(Modifier.width(90.dp).height(14.dp).shimmer(brush))
        }

        // 보증 정보 섹션 (필드 2개)
        SectionBand()
        Column(modifier = Modifier.padding(horizontal = Margin20, vertical = Margin20)) {
            Box(Modifier.width(120.dp).height(18.dp).shimmer(brush))
            Spacer(Modifier.height(Margin16))
            repeat(2) {
                Box(Modifier.width(96.dp).height(13.dp).shimmer(brush))
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth(0.5f).height(18.dp).shimmer(brush))
                Spacer(Modifier.height(Margin16))
            }
        }

        // 원본 영수증 섹션 (제목 + 썸네일 3개)
        SectionBand()
        Column(modifier = Modifier.padding(vertical = Margin20)) {
            Box(
                Modifier
                    .padding(horizontal = Margin20)
                    .width(120.dp)
                    .height(18.dp)
                    .shimmer(brush),
            )
            Spacer(Modifier.height(Margin16))
            Row(
                modifier = Modifier.padding(horizontal = Margin20),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(3) {
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .shimmer(brush, Rounded2xl),
                    )
                }
            }
        }

        Spacer(Modifier.height(Margin24))
    }
}

@Composable
private fun ReceiptDetailContent(
    receipt: ReceiptItem,
    onImageClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── 대표 이미지(카테고리/기기) 히어로 카드 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20)
                .padding(top = Margin8)
                .aspectRatio(16f / 9f)
                .clip(Rounded2xl)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE5F0FF), // Top: 옅은 블루
                            Color(0xFFF6FAFF)  // Bottom: 하얀 계열
                        )
                    )
                )
                .border(1.dp, ColorBrandQuinary, Rounded2xl),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    DeviceImage.resolve(
                        receipt.category,
                        receipt.subCategory
                    )
                ),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
            )
        }

        Spacer(Modifier.height(Margin24))
        Column(modifier = Modifier.padding(horizontal = Margin20)) {
            DetailField(
                label = stringResource(R.string.manual_product_name),
                value = receipt.itemName,
            )
            DetailField(
                label = stringResource(R.string.manual_purchase_date),
                value = receipt.paymentDate.toDotDate(),
            )
            DetailField(
                label = stringResource(R.string.receipt_detail_expiry),
                value = receipt.expiresOn.toDotDate(),
                trailing = { WarrantyDayBadge(receipt.warrantyDDay) },
            )

            Spacer(Modifier.height(Margin16))
            FieldLabel(stringResource(R.string.manual_memo))
            Spacer(Modifier.height(Margin8))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .clip(RoundedLg)
                    .background(ColorGray50)
                    .padding(Margin16),
            ) {
                val memo = receipt.memo?.takeIf { it.isNotBlank() }
                Text(
                    text = memo ?: stringResource(R.string.receipt_detail_memo_empty),
                    fontSize = 14.sp,
                    color = if (memo != null) ColorGray900 else ColorGray400,
                    lineHeight = 20.sp,
                )
            }
        }

        // ── 실물 영수증 보관 여부 ──
        SectionBand()
        Column(modifier = Modifier.padding(horizontal = Margin20, vertical = 20.dp)) {
            Text(
                text = stringResource(R.string.manual_keep_receipt_title),
                fontSize = 18.sp, // 상세 페이지는 18sp 유지
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    if (receipt.requiresPhysicalReceipt) R.string.receipt_detail_physical_kept
                    else R.string.receipt_detail_physical_not_kept
                ),
                fontSize = 15.sp,
                color = ColorBrandPrimary,
            )
        }

        // ── 보증 정보 ──
        SectionBand()
        Column(modifier = Modifier.padding(horizontal = Margin20, vertical = Margin20)) {
            Text(
                text = stringResource(R.string.manual_warranty_section),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
            )
            Spacer(Modifier.height(Margin16))
            DetailField(
                label = stringResource(R.string.manual_brand),
                value = receipt.brandName?.takeIf { it.isNotBlank() } ?: "-",
            )
            DetailField(
                label = stringResource(R.string.manual_price),
                value = receipt.totalAmount?.let { "${it.toPriceString()} 원" } ?: "-",
            )
            // 시리얼 넘버 — 라벨 옆 도움말 뱃지
            Column(modifier = Modifier.padding(vertical = Margin12)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FieldLabel(stringResource(R.string.manual_serial))
                    Spacer(Modifier.width(4.dp))
                    InfoTooltipIcon(tooltipText = stringResource(R.string.manual_serial_help))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = receipt.serialNumber?.takeIf { it.isNotBlank() } ?: "-",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorGray900,
                )
            }
        }

        // ── 원본 영수증 ──
        SectionBand()
        Column(modifier = Modifier.padding(vertical = Margin20)) {
            Text(
                text = stringResource(R.string.receipt_detail_original),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
                modifier = Modifier.padding(horizontal = Margin20),
            )
            Spacer(Modifier.height(Margin16))
            if (receipt.receiptFiles.isEmpty()) {
                Text(
                    text = stringResource(R.string.receipt_detail_original_empty),
                    fontSize = 14.sp,
                    color = ColorGray400,
                    modifier = Modifier.padding(horizontal = Margin20),
                )
            } else {
                // 상세는 읽기 전용 → X 삭제 버튼 없이(onRemove = null) 탭 시 뷰어만 연다.
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Margin20),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(receipt.receiptFiles, key = { it.fileId }) { file ->
                        val index = receipt.receiptFiles.indexOf(file)
                        ReceiptAttachmentThumbnail(
                            model = file.toContentUrl(),
                            onClick = { onImageClick(index) },
                            modifier = Modifier.size(100.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(Margin20))
            // ── 하단 CTA — 연한 파랑 링크 스타일 ──
            val supportUrl = receipt.supportUrl
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Margin20)
                    .clip(RoundedXl)
                    .background(ColorBrandSenary)
                    .clickable(enabled = !supportUrl.isNullOrBlank()) {
                        if (!supportUrl.isNullOrBlank()) {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(supportUrl)
                                    )
                                )
                            }
                        }
                    }
                    .padding(horizontal = Margin16, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.receipt_detail_support_cta),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorBrandPrimary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = ColorBrandPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(Modifier.height(Margin24))
    }
}

/** 라벨(회색) + 값(진한 색) + 하단 구분선. trailing으로 우측 요소(D-day 뱃지 등) 배치. */
@Composable
private fun DetailField(
    label: String,
    value: String,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(vertical = Margin12)) {
        FieldLabel(label)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = ColorGray900,
                modifier = Modifier.weight(1f),
            )
            if (trailing != null) {
                Spacer(Modifier.size(Margin8))
                trailing()
            }
        }
        Spacer(Modifier.height(Margin12))
        HorizontalDivider(thickness = 1.dp, color = ColorGray100)
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, fontSize = 13.sp, color = ColorGray500)
}

/** 섹션 구분 밴드 (연한 회색 8dp) */
@Composable
private fun SectionBand() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(ColorGray50),
    )
}



