package com.windrr.boat.feature.home

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.windrr.boat.R
import com.windrr.boat.ui.component.BoatNativeAdBanner
import com.windrr.boat.core.ocr.DeviceImage
import com.windrr.boat.feature.receipt.WarrantyDayBadge
import com.windrr.boat.ui.theme.BottomBarClearance
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandSecondary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorBrandTertiary
import com.windrr.boat.ui.theme.ColorGray100
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.Rounded2xl
import com.windrr.boat.ui.theme.RoundedFull
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 홈 일반 콘텐츠 — AS 만료 예정(가로형) + 가전 소모품 배너 + 최근 등록 영수증(세로형).
 */
@Composable
fun HomeGeneralContent(
    expiring: List<ExpiringWarranty>,
    expiringTotalCount: Int,
    recent: List<RecentReceipt>,
    onExpiringMore: () -> Unit = {},
    onExpiringClick: (ExpiringWarranty) -> Unit = {},
    onRecentMore: () -> Unit = {},
    onRecentClick: (RecentReceipt) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // 세로 스크롤은 상위(HomeScreenContent)에서 처리하므로 여기선 일반 Column.
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // ── AS 만료 예정 ─────────────────────────────
        Spacer(Modifier.height(Margin8))
        if (expiring.isEmpty()) {
            // 0건일 때는 캡션·타이틀·안내 메시지가 우는 보보와 함께 하나의 카드에 담긴다.
            ExpiringEmptyBanner(
                onMoreClick = onExpiringMore,
                modifier = Modifier.padding(horizontal = Margin20),
            )
        } else {
            ExpiringWarrantySection(
                expiring = expiring,
                totalCount = expiringTotalCount,
                onMoreClick = onExpiringMore,
                onItemClick = onExpiringClick,
                modifier = Modifier.padding(horizontal = Margin20),
            )
        }

        // ── 구글 네이티브 광고 (기존 AccessoryBanner 대체) ───────────────
        Spacer(Modifier.height(Margin20))
        BoatNativeAdBanner(
            modifier = Modifier
                .padding(horizontal = Margin20)
                .clip(RoundedXl)
        )

        // ── 최근 등록된 영수증 ────────────────────────
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.home_recent_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorGray900,
            modifier = Modifier.padding(horizontal = Margin20),
        )
        Spacer(Modifier.height(Margin12))
        Column(
            modifier = Modifier.padding(horizontal = Margin20),
        ) {
            recent.forEachIndexed { index, item ->
                RecentReceiptItem(item = item, onClick = { onRecentClick(item) })
                if (index != recent.lastIndex) {
                    Spacer(Modifier.height(12.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            // 더보기
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedXl)
                    .background(ColorBrandSenary)
                    .clickable(onClick = onRecentMore),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.home_more),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorBrandSecondary,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = ColorBrandSecondary,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .size(16.dp),
                )
            }
        }
        // 플로팅 하단 바에 가려지지 않도록 여유 있게 확보
        Spacer(Modifier.height(BottomBarClearance))
    }
}

/**
 * AS 만료 예정(가로형) 섹션 — [ExpiringEmptyBanner]와 동일한 배치 공식.
 * 파란 히어로 카드 하나에 헤더 + 가로 카드 캐러셀 + 인디케이터를 담고,
 * 윙크하는 보보의 몸통(img_happy_bobo)과 손+태그(img_happy_bobo_hand)를 같은 크기·위치로 겹친 뒤
 * 그 사이(z-order)에 카드 캐러셀을 그려서 손이 카드 위에 얹힌 것처럼 보이게 한다.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun ExpiringWarrantySection(
    expiring: List<ExpiringWarranty>,
    totalCount: Int,
    onMoreClick: () -> Unit,
    onItemClick: (ExpiringWarranty) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // offsetY로 캐릭터 세트 상하 위치 조정. 손 태그가 카드의 "보증종료" 텍스트를 가리지 않도록
    // 캐릭터는 위로, 카드는 아래로(헤더-카드 간격 확대) 벌린다.
    val bobo = MascotLayout(width = 90.dp, height = 127.dp, offsetY = 8.dp)
    // 카드 너비 — 다음 카드가 파란 카드 우측에 살짝 걸쳐 보이도록(peek) 산정.
    // 화면폭 - 좌우 화면 마진(40) - 캐러셀 좌측 여백(20) - 우측 peek 여백(28)
    val cardWidth = LocalConfiguration.current.screenWidthDp.dp - 88.dp

    // 노출된 카드(최대 5개)보다 전체 만료 예정 건수가 많으면, 캐러셀 끝에 "N건 더보기" 카드를 붙인다.
    val remainingMore = (totalCount - expiring.size).coerceAtLeast(0)
    val showMoreCard = remainingMore > 0
    // "더보기" 카드 높이를 일반 카드와 정확히 맞추기 위해 첫 카드의 실측 높이를 공유한다.
    var cardHeightPx by remember { mutableStateOf(0) }
    val pageCount = expiring.size + if (showMoreCard) 1 else 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(Rounded2xl)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3E82F7), Color(0xFF6FA1F8)),
                )
            ),
    ) {
        // 1) 몸통 — 맨 아래. 카드와 겹치는 부분은 카드에 가려진다. (endPadding을 키워 캐릭터를 안쪽으로)
        MascotImage(R.drawable.img_happy_bobo, bobo, endPadding = 44.dp)

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(top = Margin24, bottom = Margin20)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Margin20),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 캐릭터 세트는 안쪽으로 들여놓고(chevron과 겹치지 않게), 타이틀은 캐릭터 아래로 넘어가지 않도록 우측 여백 확보
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(end = 96.dp)) {
                    Text(
                        text = stringResource(R.string.home_expiring_caption),
                        fontSize = 13.sp,
                        color = ColorWhite.copy(alpha = 0.85f),
                    )
                    Spacer(Modifier.height(2.dp))
                    Row {
                        Text(
                            stringResource(R.string.home_expiring_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorWhite,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.home_expiring_count, totalCount),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorWhite,
                        )
                    }
                }
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = ColorWhite,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onMoreClick),
                )
            }

            Spacer(Modifier.height(30.dp)) // 헤더-카드 간격 확대 → 손 태그가 카드 텍스트를 덜 가리도록
            // 2) 카드 캐러셀 — 몸통 위, 손 아래(z-order로 몸통의 하단부를 가린다)
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = Margin20),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(expiring, key = { _, item -> item.receiptId }) { _, item ->
                    ExpiringWarrantyCard(
                        item = item,
                        width = cardWidth,
                        onClick = { onItemClick(item) },
                        // 실측 높이를 "더보기" 카드에 그대로 물려준다. 첫 카드(index 0)만 측정하면,
                        // 목록 화면에 갔다가 돌아왔을 때 스크롤이 "더보기" 카드 쪽에 남아있어 첫 카드가
                        // LazyRow 컴포지션 창 밖으로 벗어나 다시 측정되지 않고, cardHeightPx가 갱신되지
                        // 않아 "더보기" 카드가 높이 0(콘텐츠만큼만)으로 찌그러지는 문제가 있었다.
                        // 모든 카드가 동일한 높이이므로, 현재 컴포즈된 카드 아무거나 계속 갱신해도 안전하다.
                        modifier = Modifier.onSizeChanged { cardHeightPx = it.height },
                    )
                }
                if (showMoreCard) {
                    item(key = "expiring_more") {
                        ExpiringMoreCard(
                            remainingCount = remainingMore,
                            heightPx = cardHeightPx,
                            onClick = onMoreClick,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            CarouselIndicator(
                count = pageCount,
                listState = listState,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // 3) 손 + 보증 태그 — 맨 위. 몸통과 완전히 동일한 크기·위치라야 이어져 보인다.
        MascotImage(R.drawable.img_happy_bobo_hand, bobo, endPadding = 44.dp)
    }
}

/** 몸통/손 레이어에 공통으로 쓰는 크기·오프셋. 두 레이어는 항상 이 값을 그대로 공유해야 한다. */
private data class MascotLayout(val width: Dp, val height: Dp, val offsetY: Dp)

@Composable
private fun BoxScope.MascotImage(
    @DrawableRes drawable: Int,
    layout: MascotLayout,
    endPadding: Dp = Margin20
) {
    Image(
        painter = painterResource(drawable),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(end = endPadding)
            .size(width = layout.width, height = layout.height)
            .offset(y = layout.offsetY),
    )
}

/** 캐러셀 페이지 인디케이터 — 현재 보이는 카드가 넓은 필(pill), 나머지는 작은 도트. */
@SuppressLint("FrequentlyChangingValue")
@Composable
private fun CarouselIndicator(
    count: Int,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    if (count == 0) return
    val layoutInfo = listState.layoutInfo
    val activeIndex = if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
        val firstVisible = layoutInfo.visibleItemsInfo.first()
        val itemSize = firstVisible.size
        val offset = firstVisible.offset
        // 오프셋이 아이템 크기의 절반 이상이면 다음 인덱스로 간주
        if (offset < -itemSize / 2) {
            (firstVisible.index + 1).coerceIn(0, count - 1)
        } else {
            firstVisible.index.coerceIn(0, count - 1)
        }
    } else {
        0
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val active = index == activeIndex
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(width = if (active) 16.dp else 6.dp, height = 6.dp)
                    .clip(RoundedFull)
                    .background(if (active) ColorWhite else ColorWhite.copy(alpha = 0.4f)),
            )
        }
    }
}

/**
 * AS 만료 예정 가로형 카드 — 상단에 D-day 뱃지 + 보증종료일, 구분선, 하단에 썸네일/기기명/브랜드/구매일.
 * D-day 뱃지는 목록 탭과 동일한 [WarrantyDayBadge] 컴포넌트를 그대로 재사용한다.
 */
@Composable
private fun ExpiringWarrantyCard(
    item: ExpiringWarranty,
    width: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(width)
            .clip(Rounded2xl)
            .background(ColorWhite)
            .clickable(onClick = onClick)
            .padding(Margin16),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WarrantyDayBadge(warrantyDDay = item.dDay)
            Text(
                text = stringResource(R.string.home_warranty_end_label, item.expiryLabel),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorBrandPrimary,
            )
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(thickness = 1.dp, color = ColorGray100)
        Spacer(Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Thumbnail(
                category = item.category,
                subCategory = item.subCategory,
                sizeDp = 56,
                bg = ColorBrandSenary,
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                LabelValueRow(stringResource(R.string.home_label_brand), item.brand)
                Spacer(Modifier.height(6.dp))
                LabelValueRow(stringResource(R.string.home_label_purchase), item.purchaseDate)
            }
        }
    }
}

/**
 * 캐러셀 끝 "N건 더보기" 카드 — 노출된 5개 외 남은 만료 예정 건수를 안내하고, 탭하면 목록으로 이동한다.
 * 파란 히어로 위에 반투명 흰색으로 얹어 일반(흰색) 카드와 시각적으로 구분한다.
 * 높이는 [heightPx](첫 일반 카드의 실측값)에 맞춰 카드들과 정확히 같게 그린다.
 */
@Composable
private fun ExpiringMoreCard(
    remainingCount: Int,
    heightPx: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val heightModifier = if (heightPx > 0) {
        Modifier.height(with(density) { heightPx.toDp() })
    } else Modifier
    Column(
        modifier = modifier
            .then(heightModifier)
            .width(96.dp)
            .clip(Rounded2xl)
            .background(ColorWhite.copy(alpha = 0.18f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.home_expiring_more_count, remainingCount),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorWhite,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.home_more),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ColorWhite,
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = ColorWhite,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

/** 브랜드/구매일 행 — 라벨 고정폭으로 값 시작점 정렬 */
@Composable
private fun LabelValueRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = ColorLabel,
            modifier = Modifier.width(52.dp), // "Apple"과 "2025.03.13"의 시작점을 동일하게 맞추기 위한 고정폭
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = ColorLabel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 최근 등록 영수증 세로형 아이템 — 옅은 블루 배경 카드. */
@Composable
private fun RecentReceiptItem(
    item: RecentReceipt,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF2F6FC), // 디자인 가이드 카드 배경색 (옅은 쿨블루) — 흰 배경 위에서 카드 경계가 드러남
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 22.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 💡 [수정] 썸네일을 감싸는 하얀색 라운드 박스 추가
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // 내부 썸네일 크기를 56dp 박스에 맞게 약간 축소
                // 바깥 박스가 이미 흰 배경이라 Thumbnail 기본 블루 배경(ColorBrandSenary)은 끔 (iOS와 동일하게 흰색만)
                Thumbnail(
                    category = item.category,
                    subCategory = item.subCategory,
                    sizeDp = 36,
                    bg = Color.Transparent,
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // 상단: 제품명 및 D-Day
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.productName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827), // 짙은 블랙/그레이
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(Modifier.width(8.dp))

                    // 💡 [수정] D-Day 텍스트의 색상을 블루 계열로 변경하고 폰트 크기 조정
                    Text(
                        text = if (item.daysAgo <= 0) {
                            stringResource(R.string.home_days_ago_today)
                        } else {
                            stringResource(R.string.home_days_ago, item.daysAgo)
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5C9DFF) // 가이드 상의 밝은 블루 색상
                    )
                }

                Spacer(Modifier.height(6.dp))

                // 하단: 구매일 라벨 및 날짜
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.home_label_purchase),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorGray600,
                    )
                    Text(
                        text = "  |  ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorGray600,
                    )
                    Text(
                        text = item.purchaseDate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorGray600,
                    )
                }
            }
        }
    }
}

/**
 * AS 만료 예정 0건일 때 표시되는 배너 — 캡션·타이틀·안내 메시지를 파란 카드 하나에 담는다.
 *
 * 몸통(img_crying_bobo)과 손+태그(img_crying_bobo_hand)는 같은 캔버스에서 분리된 레이어라
 * 반드시 동일한 크기·위치로 겹쳐야 원본처럼 자연스럽게 이어진다(따로 어긋나게 배치하면 안 됨).
 * "손이 안내 박스 위에 얹힌" 효과는 두 레이어의 위치를 다르게 주는 게 아니라, 안내 박스를
 * 몸통과 손 사이(z-order)에 그려서 몸통의 하단은 박스에 가려지고 손은 박스 위로 드러나게 한다.
 */
@Composable
private fun ExpiringEmptyBanner(
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 💡 [교정 1] 마스코트의 총 높이를 늘려(124.dp) 배너 영역까지 뻗어나갈 수 있는 잉여 공간 확보
    val bobo = MascotLayout(width = 88.dp, height = 107.dp, offsetY = 0.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(274.dp)
            .clip(Rounded2xl)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3E82F7), Color(0xFF6FA1F8)),
                )
            )
            .padding(start = 12.dp, top = 32.dp, end = 12.dp, bottom = 16.dp),
    ) {
        // 1) 몸통 — 맨 아래. (Inner Banner에 의해 하단이 덮임)
        MascotImage(R.drawable.img_crying_bobo, bobo, endPadding = 32.dp)

        Column(modifier = Modifier.fillMaxWidth()) {

            // 💡 [교정 2] 상단 텍스트 영역의 높이를 84dp로 축소
            // 이로 인해 하단의 안내 박스가 위로 당겨지며, 마스코트(124dp)와 약 40dp 겹침(Overlap) 발생
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            ) {
                // 좌측 텍스트 (수직 중앙 정렬)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(end = 100.dp) // 마스코트 영역 침범 방지
                ) {
                    Text(
                        text = stringResource(R.string.home_expiring_caption),
                        fontSize = 13.sp,
                        color = ColorWhite.copy(alpha = 0.85f),
                    )
                    Spacer(Modifier.height(2.dp))
                    Row {
                        Text(
                            stringResource(R.string.home_expiring_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorWhite,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.home_expiring_count, 0),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorWhite,
                        )
                    }
                }

                // 우측 화살표 (수직 중앙 및 우측 끝 정렬) — 카드 우측 끝에서 22px(카드 패딩 12px + 추가 10px)
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = ColorBrandTertiary,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp)
                        .clickable(onClick = onMoreClick),
                )
            }

            Spacer(Modifier.height(28.dp))

            // 2) 안내 박스 — 몸통 위, 손 아래
            // 몸통 하단을 덮어버려 "깔끔한 절취선" 효과를 내고, 손(태그)은 이 위로 오버랩 됨
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(RoundedXl)
                    .background(ColorWhite.copy(alpha = 0.18f))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.home_expiring_empty),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorWhite,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }

        // 3) 손 + 보증 만료 태그 — 맨 위. (안내 박스 위로 오버랩 됨)
        MascotImage(R.drawable.img_crying_bobo_hand, bobo, endPadding = 32.dp)
    }
}

private val ColorLabel = Color(0xFF616161)

/** 제품 썸네일 — 실제 업로드 사진이 아닌 카테고리/소분류 기본 이미지를 보여준다. */
@Composable
private fun Thumbnail(
    category: String?,
    subCategory: String?,
    sizeDp: Int,
    bg: Color = ColorBrandSenary
) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(RoundedXl)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(DeviceImage.resolve(category, subCategory)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
