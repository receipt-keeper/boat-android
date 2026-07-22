package com.windrr.boat.feature.receipt

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.R
import com.windrr.boat.core.ocr.DeviceImage
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.feature.notification.NotificationBadgeViewModel
import com.windrr.boat.ui.component.BoatDialog
import com.windrr.boat.ui.component.BoatFilterChip
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.RefreshOnResume
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.component.rememberShimmerBrush
import com.windrr.boat.ui.component.shimmer
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
import com.windrr.boat.ui.theme.ColorSystemError
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.PretendardFontFamily
import com.windrr.boat.ui.theme.Rounded2xl
import com.windrr.boat.ui.theme.RoundedFull
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
    badgeViewModel: NotificationBadgeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val hasUnreadNotification by badgeViewModel.hasUnread.collectAsState()
    val context = LocalContext.current
    val toastState = rememberBoatToastState()
    val deleteFailedMessage = stringResource(R.string.receipt_delete_failed)
    val deletedMessage = stringResource(R.string.receipt_deleted_toast)
    val listState = rememberLazyListState()

    // 리스트 끝에서 3번째 항목 이내가 보이면 다음 페이지를 미리 불러온다. iOS loadMoreIfNeeded 대응.
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= state.receipts.size - 3) {
                    viewModel.handleIntent(ReceiptListIntent.LoadMore)
                }
            }
    }

    // 화면 진입(탭 전환 포함)마다 최신화 — 등록 후 홈 복귀 시 새 영수증 반영.
    // 홈 "만료 예정 >" 등 외부 진입이면 초기 탭/정렬을 함께 적용한다.
    LaunchedEffect(Unit) {
        if (initialTab != null || initialSort != null) {
            viewModel.handleIntent(ReceiptListIntent.ApplyInitial(initialTab, initialSort))
            onInitialConsumed()
        } else {
            viewModel.handleIntent(ReceiptListIntent.Refresh)
        }
        badgeViewModel.refresh()
    }

    // 상세/수정/등록 등 다른 화면에서 목록으로 복귀할 때마다 현재 탭/정렬 기준으로 최신화.
    // (편집/삭제/신규 등록이 별도 Activity에서 일어나므로 복귀 시 목록을 다시 불러온다)
    RefreshOnResume {
        viewModel.handleIntent(ReceiptListIntent.Refresh)
        badgeViewModel.refresh()
    }

    // 삭제 실패 시 토스트로 안내 (목록 전체를 가리는 error와는 별개)
    LaunchedEffect(state.deleteError) {
        if (state.deleteError != null) {
            toastState.showError(state.deleteError ?: deleteFailedMessage)
            viewModel.handleIntent(ReceiptListIntent.ConsumeDeleteError)
        }
    }

    // 삭제 성공 시 완료 토스트
    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            toastState.show(deletedMessage)
            viewModel.handleIntent(ReceiptListIntent.ConsumeDeleteSuccess)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (state.isLoading && state.receipts.isEmpty()) {
            ReceiptListSkeleton()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ColorGray50),
            ) {
                // 헤더 + inner tab — 흰 배경
                Column(modifier = Modifier.background(ColorWhite)) {
                    BoatHeader(
                        title = stringResource(R.string.tab_list),
                        hasUnreadNotification = hasUnreadNotification,
                        onSearchClick = onSearchClick,
                        onNotificationClick = {
                            context.startActivity(
                                Intent(
                                    context,
                                    com.windrr.boat.feature.notification.NotificationListActivity::class.java
                                )
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
                    // 칩 라인 하단 여백은 아래 72dp 간격 Spacer가 전담하므로 bottom은 0으로 둔다.
                    contentPadding = PaddingValues(start = Margin20, end = Margin20, top = 12.dp, bottom = 0.dp),
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

                // 디자인 가이드: 칩 라인과 "전체 | N ..." 라인 사이 간격 72dp
                Spacer(Modifier.height(24.dp))

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
                        state.error != null && state.receipts.isEmpty() -> {
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
                                style = TextStyle(
                                    fontFamily = PretendardFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp,
                                    lineHeight = 25.2.sp, // 18sp의 140%
                                    letterSpacing = 0.sp,
                                ),
                                color = ColorGray500,
                                // 디자인 가이드: 박스 정중앙이 아니라 상단 쪽에 위치 (64dp는 너무 붙어서 160dp로 확대)
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 160.dp),
                            )
                        }

                        else -> {
                            LazyColumn(
                                state = listState,
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
                                        onClick = {
                                            context.startActivity(
                                                ReceiptDetailActivity.intent(context, item.receiptId)
                                            )
                                        },
                                        onEdit = {
                                            context.startActivity(
                                                ReceiptEditActivity.intent(context, item.receiptId)
                                            )
                                        },
                                        onDelete = {
                                            viewModel.handleIntent(ReceiptListIntent.DeleteReceipt(item.receiptId))
                                        },
                                    )
                                }
                                if (state.isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CircularProgressIndicator(
                                                color = ColorBrandPrimary,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        BoatToastHost(state = toastState)
    }
}

/** API 로딩 중 표시되는 목록 화면 스켈레톤 — 헤더/탭/필터/리스트 전체를 셔머로 표현 */
@Composable
private fun ReceiptListSkeleton() {
    val brush = rememberShimmerBrush()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorGray50)
    ) {
        // 상단 헤더 & 탭 영역 (흰 배경)
        Column(modifier = Modifier.background(ColorWhite)) {
            // 헤더 스켈레톤 (타이틀 + 아이콘 2개)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = Margin20),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(120.dp).height(24.dp).shimmer(brush, RoundedMd))
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(24.dp).shimmer(brush, RoundedFull))
                Spacer(Modifier.width(16.dp))
                Box(Modifier.size(24.dp).shimmer(brush, RoundedFull))
            }
            // 탭 스켈레톤 (3개)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.width(48.dp).height(16.dp).shimmer(brush, RoundedMd))
                    }
                }
            }
            HorizontalDivider(color = ColorGray200)
        }

        // 카테고리 필터 칩 (가로 스크롤 형태)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) {
                Box(Modifier.width(72.dp).height(34.dp).shimmer(brush, RoundedFull))
            }
        }

        // 카운트 + 정렬
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.width(80.dp).height(14.dp).shimmer(brush, RoundedMd))
            Box(Modifier.width(60.dp).height(14.dp).shimmer(brush, RoundedMd))
        }

        // 리스트 카드 5개
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(Margin12)
        ) {
            repeat(5) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedXl,
                    color = ColorWhite,
                    border = BorderStroke(1.dp, ColorGray100)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(Modifier.size(64.dp).shimmer(brush, RoundedCornerShape(12.dp)))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.fillMaxWidth(0.6f).height(16.dp).shimmer(brush, RoundedMd))
                                Spacer(Modifier.weight(1f))
                                Box(Modifier.width(58.dp).height(26.dp).shimmer(brush, RoundedCornerShape(4.dp)))
                            }
                            Box(Modifier.fillMaxWidth(0.4f).height(13.dp).shimmer(brush, RoundedMd))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptCard(item: ReceiptItem, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenuSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedXl,
        color = ColorWhite,
        border = BorderStroke(1.dp, ColorGray100),
        shadowElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ReceiptItemThumbnail(
                    category = item.category,
                    subCategory = item.subCategory,
                    sizeDp = 50,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
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

                        Icon(
                            painter = painterResource(R.drawable.icon_more),
                            contentDescription = stringResource(R.string.common_more),
                            tint = ColorGray400,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(24.dp)
                                .clickable { showMenuSheet = true },
                        )
                    }

                    // 💡 교정 2: "AS 만료일 | 2026. 07. 16" 형식으로 타이포그래피 정교화 (Body2 Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AS 만료일", // R.string.receipt_expiry_date_label 로 유지 가능
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorGray400,
                            maxLines = 1,
                            softWrap = false,
                        )
                        Text(
                            text = "  |  ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorGray200,
                        )
                        Text(
                            text = item.expiresOn?.formatDate() ?: "-",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorGray500,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (!item.memo.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorGray50)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = item.memo,
                        fontSize = 12.sp,
                        color = ColorGray500,
                        lineHeight = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    // ── 케밥 메뉴 (수정하기 / 삭제하기 / 닫기) ──
    if (showMenuSheet) {
        ReceiptListMenuSheet(
            onDismiss = { showMenuSheet = false },
            onEdit = {
                showMenuSheet = false
                onEdit()
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
                onDelete()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

/** 케밥 클릭 시 뜨는 액션 바텀시트 — 수정하기/삭제하기 카드 + 별도 닫기 버튼 (상세 화면과 동일 구성) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptListMenuSheet(
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

/**
 * 영수증 대표 이미지 썸네일 (56x56).
 * 실제 업로드 사진이 아닌 카테고리/소분류 기본 이미지를 표시한다.
 */
@Composable
internal fun ReceiptItemThumbnail(
    category: String?,
    subCategory: String?,
    sizeDp: Int = 64,
) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(RoundedLg)
            .background(ColorGray50),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(DeviceImage.resolve(category, subCategory)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * AS 잔여일 뱃지.
 * - dDay < 0 (또는 없음): 회색 "만료"
 * - dDay == 0: 빨간 "D-Day" (만료 예정 — 만료일 당일까지는 아직 만료가 아님)
 * - 1..30: 빨간 "D-N" (임박)
 * - >30: 파란 "D-N"
 */
@Composable
internal fun WarrantyDayBadge(warrantyDDay: Int?) {
    val label: String
    val textColor: Color
    val bgColor: Color
    val borderColor: Color

    // 💡 [교정 1] 스크린샷 기반: 글자색 > 테두리색 > 배경색 순으로 미세한 명도 차이(Tint) 할당
    when {
        warrantyDDay == null || warrantyDDay < 0 -> {
            label = "만료"
            textColor = Color(0xFFA1A1AA)   // 짙은 회색
            bgColor = Color(0xFFF4F4F5)     // 아주 옅은 회색
            borderColor = Color(0xFFE4E4E7) // 중간 옅은 회색
        }
        warrantyDDay == 0 -> {
            label = "D-Day"
            textColor = Color(0xFFEF4444)   // 짙은 빨간색
            bgColor = Color(0xFFFEF2F2)     // 아주 옅은 빨간색
            borderColor = Color(0xFFFECACA) // 중간 옅은 빨간색
        }
        warrantyDDay <= 30 -> {
            label = "D-$warrantyDDay"
            textColor = Color(0xFFEF4444)   // 짙은 빨간색
            bgColor = Color(0xFFFEF2F2)     // 아주 옅은 빨간색
            borderColor = Color(0xFFFECACA) // 중간 옅은 빨간색
        }
        else -> {
            label = "D-$warrantyDDay"
            textColor = Color(0xFF3B82F6)   // 짙은 파란색
            bgColor = Color(0xFFEFF6FF)     // 아주 옅은 파란색
            borderColor = Color(0xFFBFDBFE) // 중간 옅은 파란색
        }
    }

    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = Modifier
            // 💡 [교정 2] 세로 패딩(vertical padding)을 제거하고 고정 높이(26.dp) 및 최소 너비(58.dp) 할당
            .height(26.dp)
            .widthIn(min = 58.dp)
            .background(color = bgColor, shape = shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
    }
}

/** "YYYY-MM-DD" → "YYYY. MM. DD" */
internal fun String.formatDate(): String {
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
                        fontSize = 18.sp,
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
            // 위쪽 여백은 칩 라인 뒤의 72dp Spacer가 전담하므로 top은 0으로 둔다.
            .padding(start = Margin20, end = Margin20, top = 0.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.receipt_filter_all),
                fontSize = 16.sp,
                color = ColorGray600
            )
            Text(text = "  |  ", fontSize = 16.sp, color = ColorGray300)
            Text(
                text = "$count",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorBrandPrimary
            )
        }

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { sortExpanded = true },
            ) {
                Text(
                    text = stringResource(selectedSort.labelRes),
                    fontSize = 14.sp,
                    color = ColorGray600
                )
                Icon(
                    painter = painterResource(R.drawable.chevron_down),
                    contentDescription = null,
                    tint = ColorGray600,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(width = 10.dp, height = 6.dp),
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
