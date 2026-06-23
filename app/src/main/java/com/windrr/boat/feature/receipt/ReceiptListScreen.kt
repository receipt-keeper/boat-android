package com.windrr.boat.feature.receipt

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.component.BoatFilterChip
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Rounded2xl

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

/**
 * 목록 탭 — 공통 헤더 + 보증상태 inner tab + 카테고리 필터 칩 + 카운트/정렬 + 리스트(현재 placeholder).
 */
@Composable
fun ReceiptListScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(ReceiptTab.ALL) }
    var selectedFilter by remember { mutableStateOf(ReceiptFilter.ALL) }
    val receipts = emptyList<Unit>() // TODO: 실제 영수증 데이터 연동

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorGray50),
    ) {
        // 헤더 + inner tab 은 흰 배경
        Column(modifier = Modifier.background(ColorWhite)) {
            BoatHeader(
                title = stringResource(R.string.tab_list),
                onSearchClick = { /* TODO: 검색 */ },
                onNotificationClick = { /* TODO: 알림 */ },
            )
            ReceiptInnerTabRow(selected = selectedTab, onSelected = { selectedTab = it })
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
                    selected = filter == selectedFilter,
                    onClick = { selectedFilter = filter },
                )
            }
        }

        // 카운트 + 정렬
        CountSortRow(count = receipts.size)

        // 리스트 영역 — 데이터 없으면 placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            if (receipts.isEmpty()) {
                Text(
                    text = stringResource(R.string.receipt_empty),
                    fontSize = 16.sp,
                    color = ColorGray500,
                )
            }
            // TODO: 영수증 카드 리스트
        }
    }
}

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
private fun CountSortRow(count: Int) {
    var selectedSort by remember { mutableStateOf(ReceiptSort.DEFAULT) }
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

        // 정렬 버튼 + 드롭다운 메뉴
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
                        .rotate(90f), // ">" → "v"
                )
            }

            DropdownMenu(
                expanded = sortExpanded,
                onDismissRequest = { sortExpanded = false },
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
                            selectedSort = sort
                            sortExpanded = false
                            // TODO: 정렬 적용
                        },
                    )
                }
            }
        }
    }
}
