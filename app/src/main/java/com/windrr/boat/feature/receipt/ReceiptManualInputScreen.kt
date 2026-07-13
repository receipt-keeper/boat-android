package com.windrr.boat.feature.receipt

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.windrr.boat.R
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.core.ocr.DeviceCategory
import com.windrr.boat.core.ocr.DeviceImage
import com.windrr.boat.core.util.createImageCaptureUri
import com.windrr.boat.core.util.toMultipartPart
import com.windrr.boat.data.remote.ApiErrorParser
import com.windrr.boat.data.remote.model.CreateReceiptRequest
import com.windrr.boat.data.remote.model.OcrData
import com.windrr.boat.data.repository.ReceiptRepository
import com.windrr.boat.feature.gallery.GalleryIntent
import com.windrr.boat.feature.gallery.GalleryState
import com.windrr.boat.feature.gallery.GalleryViewModel
import com.windrr.boat.ui.component.BoatDialog
import com.windrr.boat.ui.component.BoatInputField
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.FeedbackTrigger
import com.windrr.boat.ui.component.InfoTooltipIcon
import com.windrr.boat.ui.component.PhotoSourceSheet
import com.windrr.boat.ui.component.PriceVisualTransformation
import com.windrr.boat.ui.component.SyncLoadingOverlay
import com.windrr.boat.ui.component.rememberBoatToastState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandQuinary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorBrandTertiary
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

private const val ETC = "기타"

/** 대분류 → 소분류(대표 기기명) 노출 순서 (디자인 가이드 기준) */
private val SUBCATEGORIES: Map<DeviceCategory, List<String>> = mapOf(
    DeviceCategory.KITCHEN to listOf("전자레인지", "냉장고", "밥솥", "오븐", "정수기", ETC),
    DeviceCategory.LAUNDRY to listOf("세탁기", "건조기", "청소기", "로봇청소기", ETC),
    DeviceCategory.LIVING to listOf("에어컨", "선풍기", "공기청정기", "가습기", ETC),
    DeviceCategory.IT to listOf(
        "노트북", "핸드폰", "무선이어폰", "스마트워치", "데스크탑/TV",
        "카메라", "스피커", "게임기", "헤드셋", ETC,
    ),
    DeviceCategory.OTHER to listOf(ETC),
)

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
 * 영수증 직접 입력 화면.
 * OCR 성공 후 [ocrData]를 받아 각 필드를 프리필. OCR 실패(수동 진입) 시 [ocrData] = null.
 * 필수(*): 제품명 / 구매일 / 무상 AS 만료기간
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReceiptManualInputScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialPhotos: List<Uri> = emptyList(),
    ocrData: OcrData? = null,
    galleryViewModel: GalleryViewModel = viewModel(),
) {
    val context = LocalContext.current
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

    // ── 이미지 추가 (갤러리/카메라) ───────────────────────
    val singlePickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(listOf(uri))) }

    val multiPickLauncher = key(remainingSlots) {
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(remainingSlots.coerceAtLeast(2))
        ) { uris -> if (uris.isNotEmpty()) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(uris)) }
    }

    var cameraImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraImageUri
        if (success && uri != null) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(listOf(uri)))
    }

    fun launchCamera() {
        val uri = context.createImageCaptureUri()
        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (granted) launchCamera()
    }
    fun onTakePhoto() {
        if (cameraPermission.status.isGranted) launchCamera() else cameraPermission.launchPermissionRequest()
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
    val initCategory = remember(ocrData) {
        ocrData?.category?.let { c -> DeviceCategory.entries.find { it.displayName == c } }
            ?: DeviceCategory.KITCHEN   // 디자인: 기본 선택 = 첫 카테고리
    }

    // ── 폼 상태 ──────────────────────────────────────────
    var selectedCategory    by remember { mutableStateOf(initCategory) }
    var selectedSubCategory by remember { mutableStateOf(ocrData?.subCategory) }
    var productName         by remember { mutableStateOf(ocrData?.itemName.orEmpty()) }
    var purchaseDate        by remember { mutableStateOf(ocrData?.paymentDate?.normalizeDate().orEmpty()) }
    var selectedWarranty    by remember { mutableStateOf(initWarrantyIdx) }
    var customWarrantyValue by remember { mutableStateOf(initCustomValue) }
    var customWarrantyUnit  by remember { mutableStateOf(WarrantyUnit.MONTH) }
    var memo                by remember { mutableStateOf("") }
    var brand               by remember { mutableStateOf(ocrData?.brandName.orEmpty()) }
    var price               by remember { mutableStateOf(ocrData?.totalAmount?.toString().orEmpty()) }
    var serial              by remember { mutableStateOf("") }
    var keepReceipt         by remember { mutableStateOf(false) }
    var showDatePicker      by remember { mutableStateOf(false) }
    var showAddSheet        by remember { mutableStateOf(false) }
    var productInfoExpanded by remember { mutableStateOf(true) }
    var warrantyInfoExpanded by remember { mutableStateOf(true) }

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

    val isFormComplete = productName.isNotBlank() && purchaseDate.isNotBlank() && warrantyMonths != null
    val productNameAtMax = productName.length >= ITEM_NAME_MAX
    val canSubmit = isFormComplete && photos.isNotEmpty()

    // ── 등록 ──────────────────────────────────────────────
    val scope = rememberCoroutineScope()
    val toastState = rememberBoatToastState()
    val repository = remember { ReceiptRepository() }
    var isSubmitting by remember { mutableStateOf(false) }
    var showExitConfirm by rememberSaveable { mutableStateOf(false) }

    // 뒤로가기(시스템/툴바) — 확인 다이얼로그로 가로챈다.
    // OCR 결과 기반 입력이면 "분석 횟수 재차감" 경고, 직접 입력이면 "작성 중 나가기" 안내.
    BackHandler { showExitConfirm = true }

    fun submit() {
        if (isSubmitting || !canSubmit) return
        scope.launch {
            isSubmitting = true
            val parts = coroutineScope {
                photos.map { uri -> async { uri.toMultipartPart(context, "files") } }.awaitAll()
            }
            repository.uploadFiles(parts).fold(
                onSuccess = { fileIds ->
                    val request = CreateReceiptRequest(
                        itemName = productName.trim(),
                        brandName = brand.trim().ifBlank { null },
                        paymentLocation = null,
                        paymentDate = purchaseDate.replace(".", "-").trim().ifBlank { null },
                        totalAmount = price.toIntOrNull(),
                        periodMonths = warrantyMonths,
                        expiresOn = expiryDate?.replace(".", "-"),
                        category = selectedCategory.displayName,
                        subCategory = selectedSubCategory,
                        memo = memo.trim().ifBlank { null },
                        requiresPhysicalReceipt = keepReceipt,
                        receiptFileIds = fileIds,
                    )
                    repository.createReceipt(request).fold(
                        onSuccess = { item ->
                            isSubmitting = false
                            // 영수증 등록 성공 시 피드백 시트 노출 트리거
                            FeedbackTrigger.trigger()
                            context.startActivity(
                                ReceiptRegisterCompleteActivity.intent(context, item.receiptId)
                            )
                        },
                        onFailure = {
                            isSubmitting = false
                            BoatLog.e("영수증 등록 실패", it)
                            toastState.showError(ApiErrorParser.message(it))
                        },
                    )
                },
                onFailure = {
                    isSubmitting = false
                    BoatLog.e("영수증 파일 업로드 실패", it)
                    toastState.showError(ApiErrorParser.message(it))
                },
            )
        }
    }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Scaffold(
            containerColor = ColorGray50,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                if (ocrData != null) R.string.manual_title_ocr else R.string.manual_title
                            ),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorGray900,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { showExitConfirm = true }) {
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
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(Margin8))

                // ── 등록된 이미지 확인 ────────────────────
                SectionTitle(
                    stringResource(R.string.manual_image_section),
                    Modifier.padding(horizontal = Margin20),
                    required = true,
                )
                Spacer(Modifier.height(Margin12))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = Margin20),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (remainingSlots > 0) {
                        item {
                            AddImageTile(onClick = { showAddSheet = true })
                        }
                    }
                    items(photos, key = { it.toString() }) { uri ->
                        ImageThumbnail(
                            uri = uri,
                            onRemove = { galleryViewModel.handleIntent(GalleryIntent.RemovePhoto(uri)) },
                        )
                    }
                }

                Spacer(Modifier.height(Margin20))

                // ── 카테고리 ──────────────────────────────
                SectionCard {
                    Text(
                        text = stringResource(R.string.manual_category),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorGray900,
                    )
                    Spacer(Modifier.height(Margin12))
                    CategoryDropdown(
                        selected = selectedCategory,
                        onSelect = {
                            if (it != selectedCategory) {
                                selectedCategory = it
                                selectedSubCategory = null // 카테고리 바뀌면 소분류 초기화
                            }
                        },
                    )
                    Spacer(Modifier.height(Margin16))
                    // 소분류(대표 기기명) 아이콘 — 가로 스크롤. OCR로 미리 선택된 항목이 있으면 보이도록 스크롤.
                    val subCategoryListState = rememberLazyListState()
                    // 💡 OCR로 분석되어 최초 설정된 소분류만 맨 왼쪽에 고정하고, 이후 직접 변경 시에는 위치 변화 없음
                    val initialSubCategory = remember(ocrData) { ocrData?.subCategory }
                    val orderedSubCategories = remember(selectedCategory, initialSubCategory) {
                        SUBCATEGORIES[selectedCategory].orEmpty()
                            .sortedByDescending { it == initialSubCategory }
                    }

                    LaunchedEffect(selectedCategory) {
                        val idx = orderedSubCategories.indexOf(selectedSubCategory).coerceAtLeast(0)
                        subCategoryListState.scrollToItem(idx)
                    }
                    LazyRow(
                        state = subCategoryListState,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(orderedSubCategories) { sub ->
                            SubCategoryItem(
                                label = sub,
                                iconRes = DeviceImage.resolve(selectedCategory.displayName, sub),
                                selected = selectedSubCategory == sub,
                                onClick = { selectedSubCategory = if (selectedSubCategory == sub) null else sub },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Margin20))

                // ── 제품 정보 (접기) ──────────────────────
                CollapsibleCard(
                    title = stringResource(R.string.manual_product_section),
                    expanded = productInfoExpanded,
                    onToggle = { productInfoExpanded = !productInfoExpanded },
                ) {
                    BoatInputField(
                        value = productName,
                        onValueChange = { productName = it.take(ITEM_NAME_MAX) },
                        label = stringResource(R.string.manual_product_name),
                        required = true,
                        placeholder = stringResource(R.string.manual_product_name_hint),
                        isError = productNameAtMax,
                        errorText = stringResource(R.string.edit_max_length, ITEM_NAME_MAX),
                    )

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
                        selectedWarranty == null -> HintBox(stringResource(R.string.manual_warranty_hint))
                        selectedWarranty == 4 -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OutlinedTextField(
                                    value = customWarrantyValue,
                                    onValueChange = { v -> customWarrantyValue = v.filter { it.isDigit() }.take(4) },
                                    placeholder = { Text("0", color = ColorGray400, fontSize = 15.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedLg,
                                    colors = formFieldColors(),
                                )
                                WarrantyUnitChip("개월", customWarrantyUnit == WarrantyUnit.MONTH) {
                                    customWarrantyUnit = WarrantyUnit.MONTH
                                }
                                WarrantyUnitChip("년", customWarrantyUnit == WarrantyUnit.YEAR) {
                                    customWarrantyUnit = WarrantyUnit.YEAR
                                }
                            }
                        }
                        else -> FieldBox(onClick = {}) {
                            Text(
                                text = stringResource(WARRANTY_OPTION_RES[selectedWarranty!!]),
                                fontSize = 15.sp,
                                color = ColorGray900,
                            )
                        }
                    }

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

                    Spacer(Modifier.height(Margin16))
                    FieldLabel(stringResource(R.string.manual_memo), required = false)
                    Spacer(Modifier.height(Margin8))
                    OutlinedTextField(
                        value = memo,
                        onValueChange = { if (it.length <= 100) memo = it },
                        placeholder = { Text(stringResource(R.string.manual_memo_hint), color = ColorGray400, fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
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

                // ── 실물 영수증 보관 여부 (디자인 스펙 정밀 반영) ─────────
                SectionCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { keepReceipt = !keepReceipt }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.manual_keep_receipt_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorGray900,
                            )
                            Checkbox(
                                checked = keepReceipt,
                                onCheckedChange = { keepReceipt = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ColorBrandPrimary,
                                    uncheckedColor = ColorGray300,
                                    checkmarkColor = ColorWhite
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "제조사 정책에 따라 수리 시 실물 영수증이\n필요할 수 있으니, 확인 후 보관 여부를 선택해 주세요.",
                            fontSize = 14.sp,
                            color = ColorGray600,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(Modifier.height(Margin20))

                // ── 보증 정보 (접기) ──────────────────────
                CollapsibleCard(
                    title = stringResource(R.string.manual_warranty_section),
                    expanded = warrantyInfoExpanded,
                    onToggle = { warrantyInfoExpanded = !warrantyInfoExpanded },
                ) {
                    BoatInputField(
                        value = brand,
                        onValueChange = { brand = it.take(ITEM_NAME_MAX) },
                        label = stringResource(R.string.manual_brand),
                        placeholder = stringResource(R.string.manual_brand_hint),
                        isError = brand.length >= ITEM_NAME_MAX,
                        errorText = stringResource(R.string.edit_max_length, ITEM_NAME_MAX),
                    )
                    Spacer(Modifier.height(Margin16))
                    BoatInputField(
                        value = price,
                        onValueChange = { price = it.filter { c -> c.isDigit() }.take(9) },
                        label = stringResource(R.string.manual_price),
                        placeholder = stringResource(R.string.manual_price_hint),
                        keyboardType = KeyboardType.Number,
                        visualTransformation = PriceVisualTransformation(),
                        isError = price.length >= 9,
                        errorText = "최대 9,999,999,999원까지 입력 가능합니다.",
                    )
                    Spacer(Modifier.height(Margin16))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FieldLabel(stringResource(R.string.manual_serial), required = false)
                        Spacer(Modifier.width(4.dp))
                        InfoTooltipIcon(tooltipText = stringResource(R.string.manual_serial_help))
                    }
                    Spacer(Modifier.height(Margin8))
                    val serialAtMax = serial.length >= ITEM_NAME_MAX
                    OutlinedTextField(
                        value = serial,
                        onValueChange = { serial = it.take(ITEM_NAME_MAX) },
                        placeholder = { Text(stringResource(R.string.manual_serial_hint), color = ColorGray400, fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        singleLine = true,
                        shape = RoundedLg,
                        isError = serialAtMax,
                        colors = formFieldColors(),
                    )
                    if (serialAtMax) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.edit_max_length, ITEM_NAME_MAX),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorSystemError,
                        )
                    }
                }

                Spacer(Modifier.height(Margin24))
                Button(
                    onClick = {
                        if (!photos.isNotEmpty()) {
                            toastState.showError("영수증 이미지를 1장 이상 등록해 주세요.")
                        } else {
                            submit()
                        }
                    },
                    enabled = isFormComplete && !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Margin20)
                        .height(56.dp),
                    shape = RoundedXl,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorBrandPrimary,
                        contentColor = ColorWhite,
                        disabledContainerColor = ColorGray200,
                        disabledContentColor = ColorGray500,
                    ),
                ) {
                    Text(stringResource(R.string.manual_submit), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(Margin24))
            }
        }

        BoatToastHost(state = toastState)
        if (isSubmitting) {
            SyncLoadingOverlay(message = stringResource(R.string.loading_register_message))
        }
    }

    // ── 이미지 추가 메뉴 (하단 액션 시트 — 카메라 / 갤러리) ──
    if (showAddSheet) {
        PhotoSourceSheet(
            onDismiss = { showAddSheet = false },
            onCamera = { showAddSheet = false; onTakePhoto() },
            onGallery = { showAddSheet = false; onPickImages() },
        )
    }

    if (showDatePicker) {
        PurchaseDatePicker(
            onDismiss = { showDatePicker = false },
            onConfirm = { dateText -> purchaseDate = dateText; showDatePicker = false },
        )
    }

    // 뒤로가기 확인 — OCR 결과 기반 입력이면 분석 횟수 재차감 경고, 직접 입력이면 작성 중 나가기 안내
    if (showExitConfirm) {
        val isFromOcr = ocrData != null
        BoatDialog(
            title = stringResource(
                if (isFromOcr) R.string.ocr_back_confirm_title else R.string.receipt_exit_confirm_title
            ),
            message = stringResource(
                if (isFromOcr) R.string.ocr_back_confirm_message else R.string.receipt_exit_confirm_message
            ),
            confirmText = stringResource(
                if (isFromOcr) R.string.ocr_back_confirm_leave else R.string.receipt_exit_confirm_leave
            ),
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = {
                showExitConfirm = false
                onBack()
            },
            onDismiss = { showExitConfirm = false },
        )
    }
}

// ── 서브 컴포저블 ─────────────────────────────────────────────────────────────

/** 흰색 섹션 카드 (좌우 20 마진) */
@Composable
private fun SectionCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Margin20)
            .clip(Rounded2xl)
            .background(ColorWhite)
            .padding(Margin16),
        content = content,
    )
}

/** 헤더(제목 + 접기 chevron) + 펼침 시 콘텐츠를 담는 흰색 카드 */
@Composable
private fun CollapsibleCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Margin20)
            .clip(Rounded2xl)
            .background(ColorWhite)
            .padding(Margin16),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorGray900)
            Spacer(Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = ColorGray400,
                // 펼침=위(^, 270°), 접힘=아래(v, 90°)
                modifier = Modifier
                    .size(20.dp)
                    .rotate(if (expanded) 270f else 90f),
            )
        }
        if (expanded) {
            Spacer(Modifier.height(Margin16))
            content()
        }
    }
}

/** 카테고리 드롭다운 — 앵커 폭에 맞춘 펼침 목록, 선택 항목은 파란 강조 */
@Composable
private fun CategoryDropdown(
    selected: DeviceCategory,
    onSelect: (DeviceCategory) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Box(modifier = Modifier.onSizeChanged { anchorWidthPx = it.width }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedLg)
                .border(1.dp, if (expanded) ColorBrandPrimary else ColorGray300, RoundedLg)
                .clickable { expanded = true }
                .padding(horizontal = Margin16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selected.displayName,
                fontSize = 15.sp,
                color = ColorGray900,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = ColorGray600,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(if (expanded) 270f else 90f),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = ColorWhite,
            shape = RoundedLg,
            modifier = if (anchorWidthPx > 0) {
                Modifier.width(with(density) { anchorWidthPx.toDp() })
            } else Modifier,
        ) {
            DeviceCategory.entries.forEach { cat ->
                val isSelected = cat == selected
                DropdownMenuItem(
                    text = {
                        Text(
                            text = cat.displayName,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) ColorBrandPrimary else ColorGray900,
                        )
                    },
                    onClick = { onSelect(cat); expanded = false },
                    modifier = if (isSelected) Modifier.background(ColorBrandSenary) else Modifier,
                )
            }
        }
    }
}

/** 소분류(대표 기기명) 아이콘 아이템 */
@Composable
private fun SubCategoryItem(
    label: String,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedXl)
                .background(if (selected) ColorBrandQuinary else ColorGray100)
                .then(if (selected) Modifier.border(1.5.dp, ColorBrandPrimary, RoundedXl) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) ColorBrandPrimary else ColorGray600,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

/** 이미지 추가 타일 (흰 배경 + 파란 테두리 + "+ 추가하기") */
@Composable
private fun AddImageTile(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedXl)
            .background(ColorWhite)
            .border(1.dp, ColorBrandTertiary, RoundedXl)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = null,
                tint = ColorBrandPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.manual_image_add),
                fontSize = 12.sp,
                color = ColorBrandPrimary
            )
        }
    }
}

/** 업로드 이미지 썸네일 + 우상단 X 삭제 */
@Composable
private fun ImageThumbnail(uri: Uri, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(100.dp)) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(RoundedXl),
        )
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
                Text(text = "✕", color = ColorWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchaseDatePicker(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    // 💡 오늘 이후의 날짜는 선택할 수 없도록 제한
    val dpState = rememberDatePickerState(
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )
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
private fun SectionTitle(text: String, modifier: Modifier = Modifier, required: Boolean = false) {
    Row(modifier = modifier) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorGray900)
        if (required) {
            Text(" *", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorSystemError)
        }
    }
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
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        color = if (selected) ColorWhite else ColorGray600,
        modifier = Modifier
            .clip(RoundedFull)
            .background(if (selected) ColorBrandPrimary else ColorWhite)
            .border(1.dp, if (selected) ColorBrandPrimary else ColorGray200, RoundedFull)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
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
