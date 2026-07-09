package com.windrr.boat.feature.receipt

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.windrr.boat.R
import com.windrr.boat.core.ocr.DeviceImage
import com.windrr.boat.core.util.toPriceString
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.model.ReceiptFile
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.ui.component.BoatDialog
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.ImageViewerScreen
import com.windrr.boat.ui.component.InfoTooltipIcon
import com.windrr.boat.ui.component.SyncLoadingOverlay
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
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorSystemError
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Rounded2xl
import com.windrr.boat.ui.theme.RoundedFull
import com.windrr.boat.ui.theme.RoundedLg
import com.windrr.boat.ui.theme.RoundedXl
import kotlinx.coroutines.delay

/** "2024-04-12" → "2024.04.12" */
private fun String?.toDotDate(): String {
    if (this.isNullOrBlank()) return "-"
    val parts = split("-")
    return if (parts.size == 3) "${parts[0]}.${parts[1]}.${parts[2]}" else this
}

/**
 * contentPath("/api/v1/files/{id}/content")에 BASE_URL을 붙여 절대 URL로 만든다.
 * 인증(Authorization 헤더)은 Coil의 전역 ImageLoader가 자동으로 붙인다(AppCore 참고).
 */
private fun ReceiptFile.toContentUrl(): String =
    "${ApiClient.BASE_URL_PROD}${contentPath.trimStart('/')}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    receiptId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
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

    // 삭제 성공 — 토스트 표시 후 잠시 뒤 이전 화면(목록)으로 복귀
    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            toastState.show(deletedMessage)
            delay(1000)
            onBack()
        }
    }
    LaunchedEffect(state.deleteError) {
        if (state.deleteError != null) {
            toastState.showError(deleteFailedMessage)
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
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = stringResource(R.string.common_back),
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
                    state.isLoading -> CircularProgressIndicator(color = ColorBrandPrimary)
                    state.receipt == null -> {
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

                    else -> ReceiptDetailContent(
                        receipt = state.receipt!!,
                        onImageClick = { index ->
                            initialImageIndex = index
                            showImageViewer = true
                        }
                    )
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
                context.startActivity(ReceiptEditActivity.intent(context, receiptId))
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
                .background(ColorBrandSenary)
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
                trailing = { WarrantyDDayBadge(receipt.warrantyDDay) },
            )

            Spacer(Modifier.height(Margin16))
            FieldLabel(stringResource(R.string.manual_memo))
            Spacer(Modifier.height(Margin8))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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

        // ── 실물 영수증 보관 여부 (읽기 전용 표시) ──
        SectionBand()
        Column(modifier = Modifier.padding(horizontal = Margin20, vertical = Margin20)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.manual_keep_receipt_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGray900,
                )
                Spacer(Modifier.width(6.dp))
                InfoTooltipIcon(tooltipText = stringResource(R.string.manual_as_guide))
            }
            Spacer(Modifier.height(Margin12))
            ReadOnlyRadioRow(
                label = stringResource(R.string.manual_keep_receipt_yes),
                selected = receipt.requiresPhysicalReceipt,
            )
            ReadOnlyRadioRow(
                label = stringResource(R.string.manual_keep_receipt_no),
                selected = !receipt.requiresPhysicalReceipt,
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
                // TODO: 원본 사진 개별 삭제 API 확정 후 X 버튼에 실제 삭제 연동
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Margin20),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(receipt.receiptFiles, key = { it.fileId }) { file ->
                        val index = receipt.receiptFiles.indexOf(file)
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { onImageClick(index) }
                        ) {
                            AsyncImage(
                                model = file.toContentUrl(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedXl)
                                    .background(ColorGray100)
                                    .border(1.dp, ColorGray200, RoundedXl),
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "✕",
                                    color = ColorWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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

/** 읽기 전용 라디오 행 — 상세 화면에서 이미 저장된 선택값만 보여준다(수정 불가). */
@Composable
private fun ReadOnlyRadioRow(label: String, selected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = ColorBrandPrimary,
                unselectedColor = ColorGray300,
            ),
        )
        Text(text = label, fontSize = 15.sp, color = ColorGray900)
    }
}


/** 무상 AS 잔여일 뱃지 — 여유(파랑) / 만료(회색) */
@Composable
private fun WarrantyDDayBadge(dDay: Int?) {
    val (label, color) = when {
        dDay == null || dDay <= 0 -> stringResource(R.string.receipt_expired) to ColorGray400
        else -> "D-$dDay" to ColorBrandPrimary
    }
    Box(
        modifier = Modifier
            .clip(RoundedFull)
            .background(ColorBrandSenary.takeIf { color == ColorBrandPrimary } ?: ColorGray50)
            .border(1.dp, color, RoundedFull)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
