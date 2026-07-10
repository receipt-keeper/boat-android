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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import com.windrr.boat.core.ocr.DeviceImage
import com.windrr.boat.feature.receipt.WarrantyDayBadge
import com.windrr.boat.ui.theme.BottomBarClearance
import com.windrr.boat.ui.theme.ColorBadgeSafeBg
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorGray100
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorGray500
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

        // ── 가전제품 소모품/액세서리 배너 ───────────────
        Spacer(Modifier.height(Margin20))
        AccessoryBanner(
            onClick = { /* TODO: 소모품/액세서리 페이지 연결 */ },
            modifier = Modifier.padding(horizontal = Margin20),
        )

        // ── 최근 등록된 영수증 ────────────────────────
        Spacer(Modifier.height(Margin24))
        Text(
            text = stringResource(R.string.home_recent_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorGray900,
            modifier = Modifier.padding(horizontal = Margin20),
        )
        Spacer(Modifier.height(Margin12))
        Column(
            modifier = Modifier.padding(horizontal = Margin20),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            recent.forEach { item ->
                RecentReceiptItem(item = item, onClick = { onRecentClick(item) })
            }
            Spacer(Modifier.height(4.dp))
            // 더보기
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedXl)
                    .background(ColorBadgeSafeBg)
                    .clickable(onClick = onRecentMore),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.home_more),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorBrandPrimary,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = ColorBrandPrimary,
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

        Column(modifier = Modifier.fillMaxWidth().padding(top = Margin24, bottom = Margin20)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Margin20),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 캐릭터 세트는 안쪽으로 들여놓고(chevron과 겹치지 않게), 타이틀은 캐릭터 아래로 넘어가지 않도록 우측 여백 확보
                Column(modifier = Modifier.weight(1f).padding(end = 96.dp)) {
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
                items(expiring, key = { it.receiptId }) { item ->
                    ExpiringWarrantyCard(
                        item = item,
                        width = cardWidth,
                        onClick = { onItemClick(item) },
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            CarouselIndicator(
                count = expiring.size,
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
private fun BoxScope.MascotImage(@DrawableRes drawable: Int, layout: MascotLayout, endPadding: Dp = Margin20) {
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
@Composable
private fun CarouselIndicator(
    count: Int,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    if (count == 0) return
    val activeIndex = listState.firstVisibleItemIndex.coerceIn(0, count - 1)
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
        shape = Rounded2xl,
        color = ColorGray50,
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Thumbnail(category = item.category, subCategory = item.subCategory, sizeDp = 48)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.productName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorGray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (item.daysAgo <= 0) {
                            stringResource(R.string.home_days_ago_today)
                        } else {
                            stringResource(R.string.home_days_ago, item.daysAgo)
                        },
                        fontSize = 12.sp,
                        color = ColorGray400
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.home_label_purchase),
                        fontSize = 13.sp,
                        color = ColorGray500
                    )
                    Text("  |  ", fontSize = 13.sp, color = ColorGray400)
                    Text(item.purchaseDate, fontSize = 13.sp, color = ColorGray500)
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
            .clip(Rounded2xl)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3E82F7), Color(0xFF6FA1F8)),
                )
            )
            .padding(Margin20),
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

                // 우측 화살표 (수직 중앙 및 우측 끝 정렬)
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = ColorWhite,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterEnd)
                        .clickable(onClick = onMoreClick),
                )
            }

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
private fun Thumbnail(category: String?, subCategory: String?, sizeDp: Int, bg: Color = ColorBrandSenary) {
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
