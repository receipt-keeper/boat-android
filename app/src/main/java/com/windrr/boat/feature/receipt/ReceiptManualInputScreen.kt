package com.windrr.boat.feature.receipt

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.windrr.boat.R
import com.windrr.boat.core.ocr.DeviceCategory
import com.windrr.boat.feature.gallery.GalleryIntent
import com.windrr.boat.feature.gallery.GalleryState
import com.windrr.boat.feature.gallery.GalleryViewModel
import com.windrr.boat.ui.component.BoatInputField
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

private val WARRANTY_OPTIONS = listOf(
    R.string.manual_warranty_6m,
    R.string.manual_warranty_1y,
    R.string.manual_warranty_2y,
    R.string.manual_warranty_3y,
    R.string.manual_warranty_custom,
)

/**
 * 영수증 직접 입력 화면 (OCR 실패 시 진입).
 * 필수(*): 제품명 / 구매일 / 무상 AS 만료기간 — 모두 채워야 하단 CTA 활성화.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptManualInputScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialPhotos: List<Uri> = emptyList(),
    galleryViewModel: GalleryViewModel = viewModel(),
) {
    // 이미지 첨부 (갤러리 다중선택, 남은 슬롯만큼 동적 제한)
    val galleryState by galleryViewModel.state.collectAsState()
    val photos = galleryState.photos

    // 등록 화면에서 넘어온 사진을 1회 시드 (회전 후 재시드 방지)
    var seededInitial by rememberSaveable { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!seededInitial) {
            seededInitial = true
            if (initialPhotos.isNotEmpty()) {
                galleryViewModel.handleIntent(GalleryIntent.AddPhotos(initialPhotos))
            }
        }
    }
    val remainingSlots = (GalleryState.MAX_PHOTOS - photos.size).coerceAtLeast(0)

    val singlePickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(listOf(uri))) }

    val multiPickLauncher = key(remainingSlots) {
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(remainingSlots.coerceAtLeast(2))
        ) { uris -> if (uris.isNotEmpty()) galleryViewModel.handleIntent(GalleryIntent.AddPhotos(uris)) }
    }

    fun onPickImages() {
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        when {
            remainingSlots <= 0 -> Unit
            remainingSlots == 1 -> singlePickLauncher.launch(request)
            else -> multiPickLauncher.launch(request)
        }
    }

    // 폼 상태 (TODO: ViewModel + 등록 API 연동)
    var selectedCategory by remember { mutableStateOf<DeviceCategory?>(null) }
    var productName by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("") }
    var selectedWarranty by remember { mutableStateOf<Int?>(null) }
    var memo by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    var keepReceipt by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }

    // 필수 항목이 모두 채워졌는지
    val canSubmit = productName.isNotBlank() && purchaseDate.isNotBlank() && selectedWarranty != null

    Scaffold(
        containerColor = ColorGray50,
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
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = ColorGray50),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Margin20),
        ) {
            // ── 등록된 이미지 확인 ──────────────────────────
            SectionTitle(stringResource(R.string.manual_image_section))
            Spacer(Modifier.height(Margin12))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (photos.size < GalleryState.MAX_PHOTOS) {
                    item { AddImageTile(onClick = { onPickImages() }) }
                }
                items(photos) { uri ->
                    ImageThumbnail(
                        uri = uri,
                        onRemove = { galleryViewModel.handleIntent(GalleryIntent.RemovePhoto(uri)) },
                    )
                }
            }

            Spacer(Modifier.height(Margin20))
            // ── 메인 입력 카드 ──────────────────────────────
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
                    DeviceCategory.entries.forEach { category ->
                        CategoryItem(
                            label = category.displayName,
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
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

                Spacer(Modifier.height(Margin16))
                FieldLabel(stringResource(R.string.manual_purchase_date), required = true)
                Spacer(Modifier.height(Margin8))
                // 구매일 — 클릭 시 DatePicker
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
                    WARRANTY_OPTIONS.forEachIndexed { index, labelRes ->
                        WarrantyChip(
                            label = stringResource(labelRes),
                            selected = selectedWarranty == index,
                            onClick = { selectedWarranty = index },
                        )
                    }
                }
                if (selectedWarranty == null) {
                    Spacer(Modifier.height(Margin8))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedLg)
                            .background(ColorBrandSenary)
                            .padding(horizontal = Margin12, vertical = 10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.manual_warranty_hint),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorBrandPrimary,
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
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
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

            // 등록 버튼 — 고정이 아니라 콘텐츠 최하단에 자연스럽게 배치
            Spacer(Modifier.height(Margin24))
            Button(
                onClick = { /* TODO: 영수증 정보 등록 API */ },
                enabled = canSubmit,
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
                Text(stringResource(R.string.manual_submit), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(Margin16))
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

/** 구매일 DatePicker (boatDatePickerColors 적용) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchaseDatePicker(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val dpState = androidx.compose.material3.rememberDatePickerState()
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = com.windrr.boat.ui.theme.boatDatePickerColors(),
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    val millis = dpState.selectedDateMillis
                    if (millis != null) {
                        val sdf = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.KOREA)
                            .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                        onConfirm(sdf.format(java.util.Date(millis)))
                    } else onDismiss()
                },
            ) { Text(stringResource(R.string.common_confirm), color = ColorBrandPrimary) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel), color = ColorGray600)
            }
        },
    ) {
        androidx.compose.material3.DatePicker(state = dpState, colors = com.windrr.boat.ui.theme.boatDatePickerColors())
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
            Text(text = " *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorSystemError)
        }
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

/** 클릭형 필드(구매일 등) — BoatInputField와 동일한 외형(52dp / 8dp radius) */
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
    ) {
        content()
    }
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

/** 카테고리 아이템 — 아이콘 placeholder(에셋 없음) + 라벨 */
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

@Composable
private fun AddImageTile(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedXl)
            .background(ColorWhite)
            .border(1.dp, ColorBrandQuinary, RoundedXl)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "+", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ColorBrandPrimary)
            Text(text = stringResource(R.string.manual_image_add), fontSize = 13.sp, color = ColorBrandPrimary)
        }
    }
}

@Composable
private fun ImageThumbnail(uri: Uri, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(100.dp)) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedXl),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(22.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✕", color = ColorWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
