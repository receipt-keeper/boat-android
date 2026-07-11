package com.windrr.boat.feature.receipt

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.windrr.boat.R
import com.windrr.boat.core.ocr.DeviceCategory
import com.windrr.boat.core.ocr.DeviceImage
import com.windrr.boat.core.util.createImageCaptureUri
import com.windrr.boat.core.util.toMultipartPart
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.feature.gallery.GalleryIntent
import com.windrr.boat.feature.gallery.GalleryState
import com.windrr.boat.feature.gallery.GalleryViewModel
import com.windrr.boat.ui.component.BoatDialog
import com.windrr.boat.ui.component.BoatInputField
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.ImageViewerScreen
import com.windrr.boat.ui.component.InfoTooltipIcon
import com.windrr.boat.ui.component.PhotoSourceSheet
import com.windrr.boat.ui.component.PriceVisualTransformation
import com.windrr.boat.ui.component.ReceiptAttachmentThumbnail
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.component.toContentUrl
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ── 상수 (ReceiptManualInputScreen과 동일한 규칙, 파일 스코프로 별도 유지) ─────────

private enum class EditWarrantyUnit { MONTH, YEAR }

private val EDIT_WARRANTY_OPTION_RES = listOf(
    R.string.manual_warranty_6m,
    R.string.manual_warranty_1y,
    R.string.manual_warranty_2y,
    R.string.manual_warranty_3y,
    R.string.manual_warranty_custom,
)

private val EDIT_PRESET_MONTHS = listOf(6, 12, 24, 36)
private const val EDIT_ETC = "기타"

private val EDIT_SUBCATEGORIES: Map<DeviceCategory, List<String>> = mapOf(
    DeviceCategory.KITCHEN to listOf("전자레인지", "냉장고", "밥솥", "오븐", "정수기", EDIT_ETC),
    DeviceCategory.LAUNDRY to listOf("세탁기", "건조기", "청소기", "로봇청소기", EDIT_ETC),
    DeviceCategory.LIVING to listOf("에어컨", "선풍기", "공기청정기", "가습기", EDIT_ETC),
    DeviceCategory.IT to listOf(
        "노트북", "핸드폰", "무선이어폰", "스마트워치", "데스크탑/TV",
        "카메라", "스피커", "게임기", "헤드셋", EDIT_ETC,
    ),
    DeviceCategory.OTHER to listOf(EDIT_ETC),
)

private const val PRODUCT_NAME_MAX = 50
private const val BRAND_MAX = 50
private const val SERIAL_MAX = 30
private const val MEMO_MAX = 100

private fun String.editNormalizeDate(): String = replace("-", ".")

private fun editCalculateExpiryDate(purchaseDateDisplay: String, months: Int): String? = runCatching {
    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).apply { timeZone = TimeZone.getTimeZone("UTC") }
    val parsed = sdf.parse(purchaseDateDisplay) ?: return null
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        time = parsed
        add(Calendar.MONTH, months)
    }
    sdf.format(cal.time)
}.getOrNull()

/**
 * 영수증 수정 화면. receiptId로 기존 값을 조회해 폼을 프리필하고, PATCH /receipts/{id}로 저장한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptEditScreen(
    receiptId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    // 수정 저장 성공 시 호출 — 호출부(Activity)가 결과를 세팅해 상세 화면이 재조회하도록 한다.
    onSubmitted: () -> Unit = onBack,
    viewModel: ReceiptEditViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val toastState = rememberBoatToastState()
    val submitFailedMessage = stringResource(R.string.edit_submit_failed)
    val submittedMessage = stringResource(R.string.edit_submitted_toast)
    var showExitConfirm by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(receiptId) { viewModel.load(receiptId) }

    // 수정 중 뒤로가기(시스템/툴바) — 변경사항이 있을 때만 확인 다이얼로그 노출
    var hasChanges by remember { mutableStateOf(false) }
    BackHandler { if (hasChanges) showExitConfirm = true else onBack() }

    // 수정 성공 — 토스트 표시 후 잠시 뒤 이전 화면(상세)으로 복귀 (상세가 재조회하도록 결과 전달)
    LaunchedEffect(state.submitted) {
        if (state.submitted) {
            toastState.show(submittedMessage)
            kotlinx.coroutines.delay(1000)
            onSubmitted()
        }
    }
    LaunchedEffect(state.submitError) {
        if (state.submitError != null) {
            toastState.showError(state.submitError ?: submitFailedMessage)
            viewModel.consumeSubmitError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = ColorGray50,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.edit_title),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorGray900,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { if (hasChanges) showExitConfirm = true else onBack() }) {
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
                                text = state.error ?: stringResource(R.string.receipt_detail_load_failed),
                                fontSize = 14.sp,
                                color = ColorGray500,
                            )
                            TextButton(onClick = { viewModel.load(receiptId) }) {
                                Text(stringResource(R.string.receipt_list_retry), color = ColorBrandPrimary)
                            }
                        }
                    }
                    else -> ReceiptEditForm(
                        receiptId = receiptId,
                        receipt = state.receipt!!,
                        isSubmitting = state.isSubmitting,
                        onSubmit = { remainingFileIds, newPhotoParts, buildRequest ->
                            viewModel.submit(receiptId, buildRequest, remainingFileIds, newPhotoParts)
                        },
                        onChanged = { hasChanges = it }
                    )
                }
            }
        }

        BoatToastHost(state = toastState)
        if (state.isSubmitting) {
            com.windrr.boat.ui.component.SyncLoadingOverlay(message = stringResource(R.string.loading_edit_submit_message))
        }
    }

    // 작성 중 나가기 확인
    if (showExitConfirm) {
        BoatDialog(
            title = stringResource(R.string.receipt_exit_confirm_title),
            message = stringResource(R.string.receipt_exit_confirm_message),
            confirmText = stringResource(R.string.receipt_exit_confirm_leave),
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = {
                showExitConfirm = false
                onBack()
            },
            onDismiss = { showExitConfirm = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
private fun ReceiptEditForm(
    receiptId: String,
    receipt: ReceiptItem,
    isSubmitting: Boolean,
    onSubmit: (
        remainingFileIds: List<String>,
        newPhotoParts: List<MultipartBody.Part>,
        buildRequest: (List<String>) -> com.windrr.boat.data.remote.model.UpdateReceiptRequest,
    ) -> Unit,
    onChanged: (Boolean) -> Unit = {},
    galleryViewModel: GalleryViewModel = viewModel(),
) {
    val context = LocalContext.current

    // ── 이미지: 기존 원본(서버) + 신규 추가(로컬) 병행 관리 ──
    val galleryState by galleryViewModel.state.collectAsState()
    val newPhotos = galleryState.photos
    val remoteFileIds = remember { mutableStateListOf(*receipt.receiptFileIds.toTypedArray()) }
    // fileId → 실제 이미지 URL (contentPath 기반). 원본 썸네일을 실제 이미지로 렌더하기 위함.
    val remoteUrlByFileId = remember(receipt.receiptFiles) {
        receipt.receiptFiles.associate { it.fileId to it.toContentUrl() }
    }
    // 이미지 뷰어 상태 — 원본(원격) + 신규(로컬)을 순서대로 합쳐 표시
    var showImageViewer by rememberSaveable { mutableStateOf(false) }
    var viewerInitialIndex by rememberSaveable { mutableStateOf(0) }

    val singlePickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(listOf(uri))) }
    val remainingSlots = remember(newPhotos.size, remoteFileIds.size) {
        (GalleryState.MAX_PHOTOS - newPhotos.size - remoteFileIds.size).coerceAtLeast(0)
    }
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
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA) { granted ->
        if (granted) launchCamera()
    }
    fun onTakePhoto() {
        if (cameraPermission.status.isGranted) launchCamera() else cameraPermission.launchPermissionRequest()
    }

    var showAddSheet by rememberSaveable { mutableStateOf(false) }

    // ── 카테고리 ──
    val initCategory = remember(receipt) {
        DeviceCategory.entries.find { it.displayName == receipt.category } ?: DeviceCategory.KITCHEN
    }
    var selectedCategory by remember { mutableStateOf(initCategory) }
    var selectedSubCategory by remember { mutableStateOf(receipt.subCategory) }

    // ── 제품 정보 ──
    var productName by remember { mutableStateOf(receipt.itemName.take(PRODUCT_NAME_MAX)) }
    var purchaseDate by remember { mutableStateOf(receipt.paymentDate?.editNormalizeDate().orEmpty()) }
    val initWarrantyIdx = remember(receipt) {
        when (receipt.periodMonths) {
            6 -> 0; 12 -> 1; 24 -> 2; 36 -> 3
            null -> null
            else -> 4
        }
    }
    var selectedWarranty by remember { mutableStateOf(initWarrantyIdx) }
    var customWarrantyValue by remember {
        mutableStateOf(
            receipt.periodMonths?.takeIf { !EDIT_PRESET_MONTHS.contains(it) }?.toString().orEmpty()
        )
    }
    var customWarrantyUnit by remember { mutableStateOf(EditWarrantyUnit.MONTH) }
    var memo by remember { mutableStateOf(receipt.memo.orEmpty().take(MEMO_MAX)) }
    var showDatePicker by remember { mutableStateOf(false) }

    // ── 실물 영수증 보관 여부 ──
    var keepReceipt by remember { mutableStateOf<Boolean?>(receipt.requiresPhysicalReceipt) }

    // ── 보증 정보 ──
    var brand by remember { mutableStateOf(receipt.brandName.orEmpty().take(BRAND_MAX)) }
    var price by remember { mutableStateOf(receipt.totalAmount?.toString().orEmpty()) }
    var serial by remember { mutableStateOf(receipt.serialNumber.orEmpty().take(SERIAL_MAX)) }

    val warrantyMonths: Int? = when (selectedWarranty) {
        0 -> 6; 1 -> 12; 2 -> 24; 3 -> 36
        4 -> customWarrantyValue.toIntOrNull()?.takeIf { it > 0 }
            ?.let { if (customWarrantyUnit == EditWarrantyUnit.YEAR) it * 12 else it }
        else -> null
    }
    val expiryDate: String? = if (purchaseDate.isNotBlank() && warrantyMonths != null) {
        editCalculateExpiryDate(purchaseDate, warrantyMonths)
    } else null

    // ── 변경사항 감지 ──
    val originalFileIds = remember(receipt) { receipt.receiptFileIds }
    val isChanged = remember(
        selectedCategory, selectedSubCategory, productName, purchaseDate,
        selectedWarranty, customWarrantyValue, customWarrantyUnit,
        keepReceipt, brand, price, serial, newPhotos, remoteFileIds.size
    ) {
        val categoryChanged = selectedCategory.displayName != receipt.category
        val subCategoryChanged = selectedSubCategory != receipt.subCategory
        val nameChanged = productName != receipt.itemName
        val dateChanged = purchaseDate != (receipt.paymentDate?.editNormalizeDate() ?: "")
        val warrantyChanged = warrantyMonths != receipt.periodMonths
        val keepReceiptChanged = keepReceipt != receipt.requiresPhysicalReceipt
        val brandChanged = (brand.ifBlank { null }) != receipt.brandName
        val priceChanged = (price.toIntOrNull()) != receipt.totalAmount
        val serialChanged = (serial.ifBlank { null }) != receipt.serialNumber
        val photosChanged = newPhotos.isNotEmpty() || remoteFileIds.toList() != originalFileIds

        categoryChanged || subCategoryChanged || nameChanged || dateChanged ||
                warrantyChanged || keepReceiptChanged || brandChanged ||
                priceChanged || serialChanged || photosChanged
    }

    LaunchedEffect(isChanged) { onChanged(isChanged) }

    // 첨부 이미지는 수정 후에도 1장 이상 5장 이하여야 한다 (서버 스펙)
    val totalPhotoCount = remoteFileIds.size + newPhotos.size
    val canSubmit = productName.isNotBlank() && purchaseDate.isNotBlank() && warrantyMonths != null &&
        totalPhotoCount in 1..GalleryState.MAX_PHOTOS && !isSubmitting

    val editScope = rememberCoroutineScope()
    fun handleSubmit() {
        if (!canSubmit) return
        editScope.launch {
            // 신규 로컬 사진만 병렬로 읽어 멀티파트 변환 (기존 원본은 이미 업로드돼 있으므로 재전송 안 함)
            val parts = coroutineScope {
                newPhotos.map { uri -> async { uri.toMultipartPart(context, "files") } }.awaitAll()
            }
            // 신규(로컬) 이미지가 항상 앞에 오도록 순서를 합쳐서 제출
            onSubmit(remoteFileIds.toList(), parts) { finalFileIds ->
                com.windrr.boat.data.remote.model.UpdateReceiptRequest(
                    itemName = productName.trim(),
                    brandName = brand.trim().ifBlank { null },
                    serialNumber = serial.trim().ifBlank { null },
                    paymentDate = purchaseDate.replace(".", "-").trim().ifBlank { null },
                    totalAmount = price.toIntOrNull(),
                    periodMonths = warrantyMonths,
                    category = selectedCategory.displayName,
                    subCategory = selectedSubCategory,
                    memo = memo.trim().ifBlank { null },
                    requiresPhysicalReceipt = keepReceipt ?: false,
                    receiptFileIds = finalFileIds,
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = Margin8),
        ) {
            // ── 카테고리 ──
            EditSectionCard {
                Text(
                    text = stringResource(R.string.manual_category),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGray900,
                )
                Spacer(Modifier.height(Margin12))
                EditCategoryDropdown(
                    selected = selectedCategory,
                    onSelect = {
                        if (it != selectedCategory) {
                            selectedCategory = it
                            selectedSubCategory = null
                        }
                    },
                )
                Spacer(Modifier.height(Margin16))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 💡 진입 시점의 기존 소분류만 맨 왼쪽에 고정하고, 이후 직접 변경 시에는 위치 변화 없음
                    val initialSubCategory = remember(receipt) { receipt.subCategory }
                    val orderedSubCategories = EDIT_SUBCATEGORIES[selectedCategory].orEmpty()
                        .sortedByDescending { it == initialSubCategory }
                    orderedSubCategories.forEach { sub ->
                        EditSubCategoryItem(
                            label = sub,
                            iconRes = DeviceImage.resolve(selectedCategory.displayName, sub),
                            selected = selectedSubCategory == sub,
                            onClick = { selectedSubCategory = if (selectedSubCategory == sub) null else sub },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Margin20))

            // ── 제품 정보 (헤더/접기 없이 항상 펼침) ──
            EditSectionCard {
                BoatInputField(
                    value = productName,
                    onValueChange = { productName = it.take(PRODUCT_NAME_MAX) },
                    label = stringResource(R.string.manual_product_name),
                    required = true,
                    placeholder = stringResource(R.string.manual_product_name_hint),
                    isError = productName.length >= PRODUCT_NAME_MAX,
                    errorText = stringResource(R.string.edit_max_length, PRODUCT_NAME_MAX),
                )

                Spacer(Modifier.height(Margin16))
                EditFieldLabel(stringResource(R.string.manual_purchase_date), required = true)
                Spacer(Modifier.height(Margin8))
                EditFieldBox(onClick = { showDatePicker = true }) {
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
                EditFieldLabel(stringResource(R.string.manual_warranty), required = true)
                Spacer(Modifier.height(Margin8))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EDIT_WARRANTY_OPTION_RES.forEachIndexed { idx, res ->
                        EditWarrantyChip(
                            label = stringResource(res),
                            selected = selectedWarranty == idx,
                            onClick = { selectedWarranty = idx },
                        )
                    }
                }
                Spacer(Modifier.height(Margin8))
                when {
                    selectedWarranty == null -> EditHintBox(stringResource(R.string.manual_warranty_hint))
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
                                colors = editFormFieldColors(),
                            )
                            EditWarrantyUnitChip("개월", customWarrantyUnit == EditWarrantyUnit.MONTH) {
                                customWarrantyUnit = EditWarrantyUnit.MONTH
                            }
                            EditWarrantyUnitChip("년", customWarrantyUnit == EditWarrantyUnit.YEAR) {
                                customWarrantyUnit = EditWarrantyUnit.YEAR
                            }
                        }
                    }
                    else -> EditFieldBox(onClick = {}) {
                        Text(
                            text = stringResource(EDIT_WARRANTY_OPTION_RES[selectedWarranty!!]),
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
                EditFieldLabel(stringResource(R.string.manual_memo), required = false)
                Spacer(Modifier.height(Margin8))
                val memoAtMax = memo.length >= MEMO_MAX
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it.take(MEMO_MAX) },
                    placeholder = { Text(stringResource(R.string.manual_memo_hint), color = ColorGray400, fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedLg,
                    isError = memoAtMax,
                    colors = editFormFieldColors(),
                )
                if (memoAtMax) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.edit_max_length, MEMO_MAX),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorSystemError,
                    )
                }
            }

            Spacer(Modifier.height(Margin20))

            // ── 실물 영수증 보관 여부 (수정 가능) ──
            EditSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.manual_keep_receipt_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorGray900,
                    )
                    Spacer(Modifier.width(6.dp))
                    InfoTooltipIcon(tooltipText = stringResource(R.string.manual_as_guide))
                }
                Spacer(Modifier.height(Margin12))
                EditRadioRow(
                    label = stringResource(R.string.manual_keep_receipt_yes),
                    selected = keepReceipt == true,
                    onClick = { keepReceipt = true },
                )
                EditRadioRow(
                    label = stringResource(R.string.manual_keep_receipt_no),
                    selected = keepReceipt == false,
                    onClick = { keepReceipt = false },
                )
            }

            Spacer(Modifier.height(Margin20))

            // ── 보증 정보 ──
            EditSectionCard {
                Text(
                    text = stringResource(R.string.manual_warranty_section),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGray900,
                )
                Spacer(Modifier.height(Margin16))
                BoatInputField(
                    value = brand,
                    onValueChange = { brand = it.take(BRAND_MAX) },
                    label = stringResource(R.string.manual_brand),
                    placeholder = stringResource(R.string.manual_brand_hint),
                    isError = brand.length >= BRAND_MAX,
                    errorText = stringResource(R.string.edit_max_length, BRAND_MAX),
                )
                Spacer(Modifier.height(Margin16))
                BoatInputField(
                    value = price,
                    onValueChange = { price = it.filter { c -> c.isDigit() } },
                    label = stringResource(R.string.manual_price),
                    placeholder = stringResource(R.string.manual_price_hint),
                    keyboardType = KeyboardType.Number,
                    visualTransformation = PriceVisualTransformation(),
                )
                Spacer(Modifier.height(Margin16))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EditFieldLabel(stringResource(R.string.manual_serial), required = false)
                    Spacer(Modifier.width(4.dp))
                    InfoTooltipIcon(tooltipText = stringResource(R.string.manual_serial_help))
                }
                Spacer(Modifier.height(Margin8))
                OutlinedTextField(
                    value = serial,
                    onValueChange = { serial = it.take(SERIAL_MAX) },
                    placeholder = { Text(stringResource(R.string.manual_serial_hint), color = ColorGray400, fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    singleLine = true,
                    shape = RoundedLg,
                    isError = serial.length >= SERIAL_MAX,
                    colors = editFormFieldColors(),
                )
                if (serial.length >= SERIAL_MAX) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.edit_max_length, SERIAL_MAX),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorSystemError,
                    )
                }
            }

            Spacer(Modifier.height(Margin20))

            // ── 원본 영수증 (추가/삭제 가능) ──
            Column(modifier = Modifier.padding(vertical = Margin8)) {
                Text(
                    text = stringResource(R.string.receipt_detail_original),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGray900,
                    modifier = Modifier.padding(horizontal = Margin20),
                )
                Spacer(Modifier.height(Margin16))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Margin20),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { EditAddImageTile(onClick = { showAddSheet = true }) }
                    itemsIndexed(newPhotos, key = { _, uri -> "local_$uri" }) { index, uri ->
                        ReceiptAttachmentThumbnail(
                            model = uri,
                            onClick = {
                                viewerInitialIndex = index
                                showImageViewer = true
                            },
                            onRemove = if (totalPhotoCount > 1) {
                                {
                                    galleryViewModel.handleIntent(GalleryIntent.ClearError)
                                    galleryViewModel.handleIntent(GalleryIntent.RemovePhoto(uri))
                                }
                            } else null,
                            modifier = Modifier.size(100.dp),
                        )
                    }
                    itemsIndexed(remoteFileIds.toList(), key = { _, id -> "remote_$id" }) { index, fileId ->
                        ReceiptAttachmentThumbnail(
                            model = remoteUrlByFileId[fileId],
                            onClick = {
                                // 뷰어 순서 = 로컬 목록 다음에 원격 목록
                                viewerInitialIndex = newPhotos.size + index
                                showImageViewer = true
                            },
                            onRemove = if (totalPhotoCount > 1) {
                                { remoteFileIds.remove(fileId) }
                            } else null,
                            modifier = Modifier.size(100.dp),
                        )
                    }
                }
                // 서버 스펙: 첨부 이미지는 수정 후에도 1장 이상이어야 함
                if (totalPhotoCount == 0) {
                    Spacer(Modifier.height(Margin8))
                    Text(
                        text = stringResource(R.string.edit_photo_required_hint),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorSystemError,
                        modifier = Modifier.padding(horizontal = Margin20),
                    )
                }
            }

            Spacer(Modifier.height(Margin24))
            Button(
                onClick = { handleSubmit() },
                enabled = canSubmit,
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
                Text(stringResource(R.string.edit_submit), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(Margin24))
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
        EditPurchaseDatePicker(
            onDismiss = { showDatePicker = false },
            onConfirm = { dateText -> purchaseDate = dateText; showDatePicker = false },
        )
    }

    // ── 이미지 뷰어 (신규 로컬 이미지 + 원본 원격 이미지 순서대로) ──
    if (showImageViewer) {
        val viewerModels: List<Any> =
            newPhotos + remoteFileIds.mapNotNull { remoteUrlByFileId[it] }
        if (viewerModels.isNotEmpty()) {
            ImageViewerScreen(
                models = viewerModels,
                initialIndex = viewerInitialIndex.coerceIn(0, viewerModels.size - 1),
                onClose = { showImageViewer = false },
            )
        }
    }
}

// ── 서브 컴포저블 (이 화면 전용, ReceiptManualInputScreen과 이름 충돌 방지를 위해 Edit 접두) ──

@Composable
private fun EditSectionCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
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

@Composable
private fun EditCategoryDropdown(selected: DeviceCategory, onSelect: (DeviceCategory) -> Unit) {
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
            Text(text = selected.displayName, fontSize = 15.sp, color = ColorGray900, modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = ColorGray600,
                modifier = Modifier.size(18.dp).rotate(if (expanded) 270f else 90f),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = ColorWhite,
            shape = RoundedLg,
            modifier = if (anchorWidthPx > 0) Modifier.width(with(density) { anchorWidthPx.toDp() }) else Modifier,
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

@Composable
private fun EditSubCategoryItem(label: String, iconRes: Int, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(64.dp).clickable(onClick = onClick),
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
            Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(44.dp))
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

@Composable
private fun EditAddImageTile(onClick: () -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPurchaseDatePicker(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val dpState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = boatDatePickerColors(),
        confirmButton = {
            TextButton(onClick = {
                val millis = dpState.selectedDateMillis
                if (millis != null) {
                    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).apply { timeZone = TimeZone.getTimeZone("UTC") }
                    onConfirm(sdf.format(Date(millis)))
                } else onDismiss()
            }) { Text(stringResource(R.string.common_confirm), color = ColorBrandPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel), color = ColorGray600) }
        },
    ) {
        DatePicker(state = dpState, colors = boatDatePickerColors())
    }
}

@Composable
private fun EditFieldLabel(text: String, required: Boolean) {
    Row {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorGray600)
        if (required) Text(" *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorSystemError)
    }
}

@Composable
private fun EditHintBox(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedLg).background(ColorBrandSenary).padding(horizontal = Margin12, vertical = 10.dp),
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ColorBrandPrimary)
    }
}

@Composable
private fun editFormFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ColorBrandPrimary,
    unfocusedBorderColor = ColorGray300,
    errorBorderColor = ColorSystemError,
    focusedContainerColor = ColorWhite,
    unfocusedContainerColor = ColorWhite,
    errorContainerColor = ColorWhite,
    cursorColor = ColorBrandPrimary,
)

@Composable
private fun EditFieldBox(onClick: () -> Unit, content: @Composable () -> Unit) {
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
private fun EditWarrantyChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
private fun EditWarrantyUnitChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
private fun EditRadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = ColorBrandPrimary, unselectedColor = ColorGray300),
        )
        Text(text = label, fontSize = 15.sp, color = ColorGray900)
    }
}
