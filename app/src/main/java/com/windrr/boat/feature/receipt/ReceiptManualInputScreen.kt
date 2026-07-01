package com.windrr.boat.feature.receipt

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.windrr.boat.R
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.core.ocr.DeviceCategory
import com.windrr.boat.core.util.toMultipartPart
import com.windrr.boat.data.remote.model.CreateReceiptRequest
import com.windrr.boat.data.remote.model.OcrData
import com.windrr.boat.data.repository.ReceiptRepository
import com.windrr.boat.feature.gallery.GalleryIntent
import com.windrr.boat.feature.gallery.GalleryState
import com.windrr.boat.feature.gallery.GalleryViewModel
import com.windrr.boat.feature.home.HomeActivity
import com.windrr.boat.ui.component.BoatInputField
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.SyncLoadingOverlay
import com.windrr.boat.ui.component.rememberBoatToastState
import kotlinx.coroutines.launch
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandQuinary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorGray100
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray700
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorGray50
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
import com.windrr.boat.ui.theme.boatDatePickerColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ── 상수 ─────────────────────────────────────────────────────────────────────

private enum class WarrantyUnit { MONTH, YEAR }

private val WARRANTY_OPTION_RES = listOf(
    R.string.manual_warranty_6m,
    R.string.manual_warranty_1y,
    R.string.manual_warranty_2y,
    R.string.manual_warranty_3y,
    R.string.manual_warranty_custom,
)

private val PRESET_MONTHS = listOf(6, 12, 24, 36)

// ── 유틸 ─────────────────────────────────────────────────────────────────────

private fun String.normalizeDate(): String = replace("-", ".")

private fun calculateExpiryDate(purchaseDateDisplay: String, months: Int): String? = runCatching {
    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val parsed = sdf.parse(purchaseDateDisplay) ?: return null
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        time = parsed
        add(Calendar.MONTH, months)
    }
    sdf.format(cal.time)
}.getOrNull()

// ── 메인 화면 ─────────────────────────────────────────────────────────────────

/**
 * 영수증 입력하기 화면.
 * OCR 성공 후 [ocrData]를 받아 각 필드를 프리필. OCR 실패(수동 진입) 시 [ocrData] = null.
 * 필수(*): 제품명 / 구매일 / 무상 AS 만료기간
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptManualInputScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialPhotos: List<Uri> = emptyList(),
    ocrData: OcrData? = null,
    galleryViewModel: GalleryViewModel = viewModel(),
) {
    val galleryState by galleryViewModel.state.collectAsState()
    val photos = galleryState.photos
    val remainingSlots = (GalleryState.MAX_PHOTOS - photos.size).coerceAtLeast(0)

    // 이전 화면 사진 1회 시드 (회전 후 재시드 방지)
    var seededInitial by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!seededInitial) {
            seededInitial = true
            if (initialPhotos.isNotEmpty()) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(initialPhotos))
        }
    }

    val singlePickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(listOf(uri))) }

    val multiPickLauncher = key(remainingSlots) {
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(remainingSlots.coerceAtLeast(2))
        ) { uris -> if (uris.isNotEmpty()) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(uris)) }
    }

    fun onPickImages() {
        val req = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        when {
            remainingSlots <= 0 -> Unit
            remainingSlots == 1 -> singlePickLauncher.launch(req)
            else -> multiPickLauncher.launch(req)
        }
    }

    // ── OCR 프리필 초기값 ────────────────────────────────
    val initWarrantyIdx = remember(ocrData) {
        when (ocrData?.periodMonths) {
            6    -> 0
            12   -> 1
            24   -> 2
            36   -> 3
            null -> null
            else -> 4
        }
    }
    val initCustomValue = remember(ocrData) {
        val m = ocrData?.periodMonths
        if (m != null && !PRESET_MONTHS.contains(m)) m.toString() else ""
    }

    // ── 폼 상태 ──────────────────────────────────────────
    var selectedCategory    by remember { mutableStateOf(ocrData?.category?.let { c -> DeviceCategory.entries.find { it.name == c } }) }
    var productName         by remember { mutableStateOf(ocrData?.itemName.orEmpty()) }
    var purchaseDate        by remember { mutableStateOf(ocrData?.paymentDate?.normalizeDate().orEmpty()) }
    var selectedWarranty    by remember { mutableStateOf(initWarrantyIdx) }
    var customWarrantyValue by remember { mutableStateOf(initCustomValue) }
    var customWarrantyUnit  by remember { mutableStateOf(WarrantyUnit.MONTH) }
    var memo                by remember { mutableStateOf("") }
    var brand               by remember { mutableStateOf(ocrData?.brandName.orEmpty()) }
    var price               by remember { mutableStateOf(ocrData?.totalAmount?.toString().orEmpty()) }
    var serial              by remember { mutableStateOf("") }
    var keepReceipt         by remember { mutableStateOf(true) }
    var showDatePicker      by remember { mutableStateOf(false) }

    // ── 파생 상태 ─────────────────────────────────────────
    val warrantyMonths: Int? = when (selectedWarranty) {
        0    -> 6
        1    -> 12
        2    -> 24
        3    -> 36
        4    -> customWarrantyValue.toIntOrNull()?.takeIf { it > 0 }?.let {
                    if (customWarrantyUnit == WarrantyUnit.YEAR) it * 12 else it
                }
        else -> null
    }
    val expiryDate: String? = if (purchaseDate.isNotBlank() && warrantyMonths != null)
        calculateExpiryDate(purchaseDate, warrantyMonths)
    else null

    val canSubmit = productName.isNotBlank() && purchaseDate.isNotBlank() && warrantyMonths != null

    // ── 등록 (파일 업로드 → 영수증 생성 → 로컬 캐시) ─────────
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toastState = rememberBoatToastState()
    val repository = remember { ReceiptRepository() }
    var isSubmitting by remember { mutableStateOf(false) }
    val failMessage = stringResource(R.string.receipt_register_done_failed)

    fun submit() {
        if (isSubmitting || !canSubmit) return
        scope.launch {
            isSubmitting = true
            // 1) 첨부 이미지 업로드 → fileId 수집
            val parts = photos.map { it.toMultipartPart(context, "files") }
            repository.uploadFiles(parts).fold(
                onSuccess = { fileIds ->
                    // 2) 입력값 + fileId로 영수증 생성
                    val request = CreateReceiptRequest(
                        itemName = productName.trim(),
                        brandName = brand.trim().ifBlank { null },
                        paymentLocation = null,
                        paymentDate = purchaseDate.replace(".", "-").trim().ifBlank { null },
                        totalAmount = price.toIntOrNull(),
                        periodMonths = warrantyMonths,
                        category = selectedCategory?.displayName,
                        subCategory = null,
                        memo = memo.trim().ifBlank { null },
                        requiresPhysicalReceipt = keepReceipt,
                        receiptFileIds = fileIds,
                    )
                    repository.createReceipt(request).fold(
                        onSuccess = {
                            isSubmitting = false
                            // 홈으로 복귀 — 등록/OCR 화면 정리, 목록은 onResume에서 갱신
                            context.startActivity(
                                Intent(context, HomeActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                            )
                        },
                        onFailure = {
                            isSubmitting = false
                            BoatLog.e("영수증 등록 실패", it)
                            toastState.showError(failMessage)
                        },
                    )
                },
                onFailure = {
                    isSubmitting = false
                    BoatLog.e("영수증 파일 업로드 실패", it)
                    toastState.showError(failMessage)
                },
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
    Scaffold(
        containerColor = ColorGray50,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.manual_title),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorGray900,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.common_back),
                            tint = Color.Unspecified,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorGray50),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Margin20),
        ) {

            // ── 등록된 이미지 확인 ────────────────────────
            SectionTitle(stringResource(R.string.manual_image_section))
            Spacer(Modifier.height(Margin12))
            if (photos.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { photos.size })
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(Rounded2xl),
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        AsyncImage(
                            model = photos[page],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    // 페이지 인디케이터
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedFull)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${photos.size}",
                            color = ColorWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            } else {
                // 사진 없는 경우 — 추가 유도 플레이스홀더
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(Rounded2xl)
                        .background(ColorGray100)
                        .border(1.dp, ColorGray200, Rounded2xl)
                        .clickable { onPickImages() },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("+", fontSize = 28.sp, color = ColorGray400, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.manual_image_add), fontSize = 13.sp, color = ColorGray400)
                    }
                }
            }

            Spacer(Modifier.height(Margin20))
            // ── 메인 입력 카드 ────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Rounded2xl)
                    .background(ColorWhite)
                    .padding(Margin16),
            ) {
                // 카테고리
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DeviceCategory.entries.forEach { cat ->
                        CategoryItem(
                            label = cat.displayName,
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(Modifier.height(Margin20))
                BoatInputField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = stringResource(R.string.manual_product_name),
                    required = true,
                    placeholder = stringResource(R.string.manual_product_name_hint),
                )

                // 구매일
                Spacer(Modifier.height(Margin16))
                FieldLabel(stringResource(R.string.manual_purchase_date), required = true)
                Spacer(Modifier.height(Margin8))
                FieldBox(onClick = { showDatePicker = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (purchaseDate.isNotBlank()) {
                            Icon(
                                painter = painterResource(R.drawable.ic_calendar),
                                contentDescription = null,
                                tint = ColorGray700,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(Margin8))
                        }
                        Text(
                            text = purchaseDate.ifBlank { stringResource(R.string.manual_purchase_date_hint) },
                            fontSize = 15.sp,
                            color = if (purchaseDate.isBlank()) ColorGray400 else ColorGray900,
                        )
                    }
                }

                // 무상 AS 만료기간
                Spacer(Modifier.height(Margin16))
                FieldLabel(stringResource(R.string.manual_warranty), required = true)
                Spacer(Modifier.height(Margin8))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WARRANTY_OPTION_RES.forEachIndexed { idx, res ->
                        WarrantyChip(
                            label = stringResource(res),
                            selected = selectedWarranty == idx,
                            onClick = { selectedWarranty = idx },
                        )
                    }
                }
                Spacer(Modifier.height(Margin8))
                when {
                    selectedWarranty == null -> {
                        // 미선택 안내
                        HintBox(stringResource(R.string.manual_warranty_hint))
                    }
                    selectedWarranty == 4 -> {
                        // 직접입력 — 숫자 + 단위 토글
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = customWarrantyValue,
                                onValueChange = { v ->
                                    customWarrantyValue = v.filter { it.isDigit() }.take(4)
                                },
                                placeholder = { Text("0", color = ColorGray400, fontSize = 15.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedLg,
                                colors = formFieldColors(),
                            )
                            WarrantyUnitChip(
                                label = "개월",
                                selected = customWarrantyUnit == WarrantyUnit.MONTH,
                                onClick = { customWarrantyUnit = WarrantyUnit.MONTH },
                            )
                            WarrantyUnitChip(
                                label = "년",
                                selected = customWarrantyUnit == WarrantyUnit.YEAR,
                                onClick = { customWarrantyUnit = WarrantyUnit.YEAR },
                            )
                        }
                    }
                    else -> {
                        // 프리셋 선택 — 값 표시 박스
                        FieldBox(onClick = {}) {
                            Text(
                                text = stringResource(WARRANTY_OPTION_RES[selectedWarranty!!]),
                                fontSize = 15.sp,
                                color = ColorGray900,
                            )
                        }
                    }
                }

                // 무상 AS 만료일 계산 배너
                if (expiryDate != null) {
                    Spacer(Modifier.height(Margin8))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedLg)
                            .background(ColorBrandSenary)
                            .padding(horizontal = Margin12, vertical = 12.dp),
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = ColorBrandPrimary, fontSize = 14.sp)) {
                                    append("무상 AS 만료일  ")
                                }
                                withStyle(SpanStyle(color = ColorBrandPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                                    append(expiryDate)
                                }
                            },
                        )
                    }
                }

                // 메모
                Spacer(Modifier.height(Margin16))
                FieldLabel(stringResource(R.string.manual_memo), required = false)
                Spacer(Modifier.height(Margin8))
                OutlinedTextField(
                    value = memo,
                    onValueChange = { if (it.length <= 100) memo = it },
                    placeholder = {
                        Text(
                            stringResource(R.string.manual_memo_hint),
                            color = ColorGray400,
                            fontSize = 15.sp,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedLg,
                    colors = formFieldColors(),
                    supportingText = {
                        Text(
                            text = stringResource(R.string.manual_memo_counter),
                            fontSize = 12.sp,
                            color = ColorGray400,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    },
                )
            }

            Spacer(Modifier.height(Margin20))
            // ── 보증 정보 ──────────────────────────────────
            SectionTitle(stringResource(R.string.manual_warranty_section))
            Spacer(Modifier.height(Margin12))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Rounded2xl)
                    .background(ColorWhite)
                    .padding(Margin16),
            ) {
                BoatInputField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = stringResource(R.string.manual_brand),
                    placeholder = stringResource(R.string.manual_brand_hint),
                )
                Spacer(Modifier.height(Margin16))
                BoatInputField(
                    value = price,
                    onValueChange = { price = it.filter { c -> c.isDigit() } },
                    label = stringResource(R.string.manual_price),
                    placeholder = stringResource(R.string.manual_price_hint),
                    keyboardType = KeyboardType.Number,
                )
                Spacer(Modifier.height(Margin16))
                BoatInputField(
                    value = serial,
                    onValueChange = { serial = it },
                    label = stringResource(R.string.manual_serial),
                    placeholder = stringResource(R.string.manual_serial_hint),
                )
            }

            Spacer(Modifier.height(Margin20))
            // ── 무상 AS 안내 ───────────────────────────────
            SectionTitle(stringResource(R.string.manual_as_section))
            Spacer(Modifier.height(Margin12))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Rounded2xl)
                    .background(ColorWhite)
                    .padding(Margin16),
            ) {
                Text(
                    text = stringResource(R.string.manual_as_guide),
                    fontSize = 13.sp,
                    color = ColorGray600,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(Margin12))
                Row(
                    modifier = Modifier.clickable { keepReceipt = !keepReceipt },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = keepReceipt,
                        onCheckedChange = { keepReceipt = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ColorBrandPrimary,
                            uncheckedColor = ColorGray300,
                            checkmarkColor = ColorWhite,
                        ),
                    )
                    Text(
                        text = stringResource(R.string.manual_as_keep_receipt),
                        fontSize = 14.sp,
                        color = ColorGray700,
                    )
                }
            }

            Spacer(Modifier.height(Margin24))
            Button(
                onClick = { submit() },
                enabled = canSubmit && !isSubmitting,
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
                    stringResource(R.string.manual_submit),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(Margin16))
        }
    }

        BoatToastHost(state = toastState)
        if (isSubmitting) {
            SyncLoadingOverlay(message = stringResource(R.string.loading_register_message))
        }
    }

    if (showDatePicker) {
        PurchaseDatePicker(
            onDismiss = { showDatePicker = false },
            onConfirm = { dateText ->
                purchaseDate = dateText
                showDatePicker = false
            },
        )
    }
}

// ── 서브 컴포저블 ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchaseDatePicker(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val dpState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = boatDatePickerColors(),
        confirmButton = {
            TextButton(onClick = {
                val millis = dpState.selectedDateMillis
                if (millis != null) {
                    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
                        .apply { timeZone = TimeZone.getTimeZone("UTC") }
                    onConfirm(sdf.format(Date(millis)))
                } else onDismiss()
            }) { Text(stringResource(R.string.common_confirm), color = ColorBrandPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel), color = ColorGray600)
            }
        },
    ) {
        DatePicker(state = dpState, colors = boatDatePickerColors())
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorGray900)
}

@Composable
private fun FieldLabel(text: String, required: Boolean) {
    Row {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorGray600)
        if (required) {
            Text(" *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorSystemError)
        }
    }
}

@Composable
private fun HintBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedLg)
            .background(ColorBrandSenary)
            .padding(horizontal = Margin12, vertical = 10.dp),
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ColorBrandPrimary)
    }
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ColorBrandPrimary,
    unfocusedBorderColor = ColorGray300,
    focusedContainerColor = ColorWhite,
    unfocusedContainerColor = ColorWhite,
    cursorColor = ColorBrandPrimary,
)

@Composable
private fun FieldBox(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedLg)
            .border(1.dp, ColorGray300, RoundedLg)
            .clickable(onClick = onClick)
            .padding(horizontal = Margin16),
        contentAlignment = Alignment.CenterStart,
    ) { content() }
}

@Composable
private fun WarrantyChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = if (selected) ColorBrandPrimary else ColorGray700,
        modifier = Modifier
            .clip(RoundedFull)
            .border(1.dp, if (selected) ColorBrandPrimary else ColorGray300, RoundedFull)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun WarrantyUnitChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = if (selected) ColorBrandPrimary else ColorGray600,
        modifier = Modifier
            .clip(RoundedFull)
            .border(1.5.dp, if (selected) ColorBrandPrimary else ColorGray300, RoundedFull)
            .background(if (selected) ColorBrandSenary else ColorWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun CategoryItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedXl)
                .background(if (selected) ColorBrandQuinary else ColorGray100)
                .then(
                    if (selected) Modifier.border(1.5.dp, ColorBrandPrimary, RoundedXl) else Modifier
                ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) ColorBrandPrimary else ColorGray600,
            maxLines = 1,
        )
    }
}
