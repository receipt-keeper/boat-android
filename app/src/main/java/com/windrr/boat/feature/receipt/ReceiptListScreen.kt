package com.windrr.boat.feature.receipt

import android.content.Intent
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.windrr.boat.R
import com.windrr.boat.core.ocr.DeviceImage
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.ui.component.BoatFilterChip
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.BottomBarClearance
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray100
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Rounded2xl
import com.windrr.boat.ui.theme.RoundedLg
import com.windrr.boat.ui.theme.RoundedMd
import com.windrr.boat.ui.theme.RoundedXl

/** 목록 상단 inner tab — 보증 상태 기준 */
enum class ReceiptTab(@StringRes val titleRes: Int) {
    ALL(R.string.receipt_tab_all),
    EXPIRING(R.string.receipt_tab_expiring),
    EXPIRED(R.string.receipt_tab_expired),
}

/** 카테고리 필터 칩 */
enum class ReceiptFilter(@StringRes val labelRes: Int) {
    ALL(R.string.receipt_filter_all),
    IT(R.string.receipt_filter_it),
    LAUNDRY(R.string.receipt_filter_laundry),
    KITCHEN(R.string.receipt_filter_kitchen),
    LIVING(R.string.receipt_filter_living),
    OTHER(R.string.receipt_filter_other),
}

/** 정렬 옵션 */
enum class ReceiptSort(@StringRes val labelRes: Int) {
    DEFAULT(R.string.receipt_sort_default),
    EXPIRING(R.string.receipt_sort_expiring),
    RECENT(R.string.receipt_sort_recent),
    PURCHASE(R.string.receipt_sort_purchase),
}

@Composable
fun ReceiptListScreen(
    modifier: Modifier = Modifier,
    initialTab: ReceiptTab? = null,
    initialSort: ReceiptSort? = null,
    onInitialConsumed: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    viewModel: ReceiptListViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val toastState = rememberBoatToastState()
    val deleteFailedMessage = stringResource(R.string.receipt_delete_failed)

    // 화면 진입(탭 전환 포함)마다 최신화 — 등록 후 홈 복귀 시 새 영수증 반영.
    // 홈 "만료 예정 >" 등 외부 진입이면 초기 탭/정렬을 함께 적용한다.
    LaunchedEffect(Unit) {
        if (initialTab != null || initialSort != null) {
            viewModel.handleIntent(ReceiptListIntent.ApplyInitial(initialTab, initialSort))
            onInitialConsumed()
        } else {
            viewModel.handleIntent(ReceiptListIntent.Refresh)
        }
    }

    // 삭제 실패 시 토스트로 안내 (목록 전체를 가리는 error와는 별개)
    LaunchedEffect(state.deleteError) {
        if (state.deleteError != null) {
            toastState.showError(deleteFailedMessage)
            viewModel.handleIntent(ReceiptListIntent.ConsumeDeleteError)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorGray50),
        ) {
            // 헤더 + inner tab — 흰 배경
            Column(modifier = Modifier.background(ColorWhite)) {
                BoatHeader(
                    title = stringResource(R.string.tab_list),
                    onSearchClick = onSearchClick,
                    onNotificationClick = {
                        context.startActivity(
                            Intent(context, com.windrr.boat.feature.notification.NotificationListActivity::class.java)
                        )
                    },
                )
                ReceiptInnerTabRow(
                    selected = state.selectedTab,
                    onSelected = { viewModel.handleIntent(ReceiptListIntent.SelectTab(it)) },
                )
            }

            // 카테고리 필터 칩 (가로 스크롤)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = Margin20, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ReceiptFilter.entries) { filter ->
                    BoatFilterChip(
                        label = stringResource(filter.labelRes),
                        selected = filter == state.selectedFilter,
                        onClick = { viewModel.handleIntent(ReceiptListIntent.SelectFilter(filter)) },
                    )
                }
            }

            // 카운트 + 정렬
            CountSortRow(
                count = state.totalCount,
                selectedSort = state.selectedSort,
                onSortSelected = { viewModel.handleIntent(ReceiptListIntent.SelectSort(it)) },
            )

            // 리스트 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(color = ColorBrandPrimary)
                    }
                    state.error != null -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error!!,
                                fontSize = 14.sp,
                                color = ColorGray500,
                            )
                            TextButton(onClick = { viewModel.handleIntent(ReceiptListIntent.Refresh) }) {
                                Text(
                                    text = stringResource(R.string.receipt_list_retry),
                                    color = ColorBrandPrimary,
                                )
                            }
                        }
                    }
                    state.receipts.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.receipt_empty),
                            fontSize = 16.sp,
                            color = ColorGray500,
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            // 플로팅 하단 바에 가려지지 않도록 하단 여백을 넉넉히 확보
                            contentPadding = PaddingValues(
                                start = Margin20,
                                end = Margin20,
                                top = 12.dp,
                                bottom = BottomBarClearance,
                            ),
                            verticalArrangement = Arrangement.spacedBy(Margin12),
                        ) {
                            items(state.receipts, key = { it.receiptId }) { item ->
                                ReceiptCard(
                                    item = item,
                                    onDelete = {
                                        viewModel.handleIntent(ReceiptListIntent.DeleteReceipt(item.receiptId))
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        BoatToastHost(state = toastState)
    }
}

// ── 영수증 카드 ───────────────────────────────────────────────────────────────

@Composable
private fun ReceiptCard(item: ReceiptItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedXl,
        colors = CardDefaults.cardColors(containerColor = ColorWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            // 상단: 썸네일 + [이름·D-day 뱃지·더보기 / 만료일]
            // 뱃지·케밥을 제목과 같은 줄에 두어 만료일 행이 전체 폭을 사용하도록 함 (iOS 구조와 일치)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReceiptItemThumbnail(
                    imageUrl = item.imageUrl,
                    category = item.category,
                    subCategory = item.subCategory,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // 제목 + D-day 뱃지 + 케밥 (한 줄)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.itemName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorGray900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        WarrantyDayBadge(warrantyDDay = item.warrantyDDay)

                        var menuExpanded by remember { mutableStateOf(false) }
                        Box {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                tint = ColorGray500,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(20.dp)
                                    .clickable { menuExpanded = true },
                            )
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                containerColor = ColorWhite,
                                shape = Rounded2xl,
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.receipt_delete),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFFF4444),
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onDelete()
                                    },
                                )
                            }
                        }
                    }

                    // 만료일 | 날짜 (전체 폭 사용 → 줄바꿈 없음)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.receipt_expiry_date_label),
                            fontSize = 13.sp,
                            color = ColorGray500,
                            maxLines = 1,
                            softWrap = false,
                        )
                        Text(text = "  |  ", fontSize = 13.sp, color = ColorGray300)
                        Text(
                            text = item.expiresOn?.formatDate() ?: "-",
                            fontSize = 13.sp,
                            color = ColorGray500,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            HorizontalDivider(color = ColorGray200)

            // 하단: 메모
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorGray50)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = item.memo?.ifBlank { "-" } ?: "-",
                    fontSize = 14.sp,
                    color = ColorGray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * 영수증 대표 이미지 썸네일 (56x56).
 * imageUrl(사용자 업로드 사진)이 있으면 그 이미지를, 없으면 카테고리/기기 기본 이미지를 표시.
 */
@Composable
private fun ReceiptItemThumbnail(
    imageUrl: String?,
    category: String?,
    subCategory: String?,
) {
    val resolvedUrl = imageUrl?.let {
        if (it.startsWith("http")) it
        else "${ApiClient.BASE_URL_PROD}${it.trimStart('/')}"
    }
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedLg)
            .background(ColorGray100),
        contentAlignment = Alignment.Center,
    ) {
        if (resolvedUrl != null) {
            AsyncImage(
                model = resolvedUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // 업로드 이미지가 없으면 카테고리/기기 기본 이미지로 폴백
            Image(
                painter = painterResource(DeviceImage.resolve(category, subCategory)),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * AS 잔여일 뱃지.
 * - dDay <= 0: 회색 "만료"
 * - 1..30: 빨간 "D-N" (임박)
 * - >30: 파란 "D-N"
 */
@Composable
private fun WarrantyDayBadge(warrantyDDay: Int?) {
    val (label, color) = when {
        warrantyDDay == null || warrantyDDay <= 0 -> "만료" to ColorGray400
        warrantyDDay <= 30 -> "D-$warrantyDDay" to Color(0xFFFF4444)
        else -> "D-$warrantyDDay" to ColorBrandPrimary
    }
    Box(
        modifier = Modifier
            .border(1.dp, color, RoundedMd)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

/** "YYYY-MM-DD" → "YYYY. MM. DD" */
private fun String.formatDate(): String {
    val parts = split("-")
    return if (parts.size == 3) "${parts[0]}. ${parts[1]}. ${parts[2]}" else this
}

// ── 내부 컴포넌트 ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptInnerTabRow(
    selected: ReceiptTab,
    onSelected: (ReceiptTab) -> Unit,
) {
    TabRow(
        selectedTabIndex = selected.ordinal,
        containerColor = ColorWhite,
        contentColor = ColorGray900,
        indicator = { positions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(positions[selected.ordinal]),
                height = 2.dp,
                color = ColorGray900,
            )
        },
        divider = { HorizontalDivider(color = ColorGray200) },
    ) {
        ReceiptTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Tab(
                selected = isSelected,
                onClick = { onSelected(tab) },
                selectedContentColor = ColorGray900,
                unselectedContentColor = ColorGray500,
                text = {
                    Text(
                        text = stringResource(tab.titleRes),
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                },
            )
        }
    }
}

@Composable
private fun CountSortRow(
    count: Int,
    selectedSort: ReceiptSort,
    onSortSelected: (ReceiptSort) -> Unit,
) {
    var sortExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Margin20, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // 전체 | N
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.receipt_filter_all), fontSize = 14.sp, color = ColorGray600)
            Text(text = "  |  ", fontSize = 14.sp, color = ColorGray300)
            Text(text = "$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorBrandPrimary)
        }

        // 정렬 버튼 + 드롭다운
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { sortExpanded = true },
            ) {
                Text(text = stringResource(selectedSort.labelRes), fontSize = 14.sp, color = ColorGray600)
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = ColorGray600,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(14.dp)
                        .rotate(90f),
                )
            }

            DropdownMenu(
                expanded = sortExpanded,
                onDismissRequest = { sortExpanded = false },
                modifier = Modifier.width(176.dp),
                containerColor = ColorWhite,
                shape = Rounded2xl,
            ) {
                ReceiptSort.entries.forEach { sort ->
                    val isSelected = sort == selectedSort
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(sort.labelRes),
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ColorGray900 else ColorGray500,
                            )
                        },
                        onClick = {
                            onSortSelected(sort)
                            sortExpanded = false
                        },
                    )
                }
            }
        }
    }
}
