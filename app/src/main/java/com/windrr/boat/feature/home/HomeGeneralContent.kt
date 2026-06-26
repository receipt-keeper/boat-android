package com.windrr.boat.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandQuaternary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorGray100
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.Rounded2xl
import com.windrr.boat.ui.theme.RoundedFull
import com.windrr.boat.ui.theme.RoundedLg
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 홈 일반 콘텐츠 — AS 만료 예정(가로형) + 최근 등록 영수증(세로형).
 */
@Composable
fun HomeGeneralContent(
    expiring: List<ExpiringWarranty>,
    recent: List<RecentReceipt>,
    onExpiringMore: () -> Unit = {},
    onExpiringClick: (ExpiringWarranty) -> Unit = {},
    onRecentMore: () -> Unit = {},
    onRecentClick: (RecentReceipt) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── AS 만료 예정 헤더 ─────────────────────────
        Spacer(Modifier.height(Margin16))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_expiring_caption),
                    fontSize = 13.sp,
                    color = ColorGray500,
                )
                Spacer(Modifier.height(2.dp))
                Row {
                    Text(stringResource(R.string.home_expiring_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorGray900)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.home_expiring_count, expiring.size),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorBrandPrimary,
                    )
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = ColorGray400,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onExpiringMore),
            )
        }

        Spacer(Modifier.height(Margin16))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Margin20),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(expiring, key = { it.id }) { item ->
                ExpiringWarrantyCard(item = item, onClick = { onExpiringClick(item) })
            }
        }

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedXl)
                    .background(ColorGray100)
                    .clickable(onClick = onRecentMore),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.home_more), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = ColorGray600)
            }
        }
        Spacer(Modifier.height(Margin16))
    }
}

private val ExpiringCardWidth = 334.dp
private val ExpiringCardHeight = 183.dp
private val DdayBadgeWidth = 80.dp
private val DdayBadgeHeight = 30.dp
// D-day 뱃지 절반이 카드 위로 겹치도록 확보하는 상단 여백
private val DdayOverhang = 15.dp

/** AS 만료 예정 가로형 카드 (334×183, D-day 뱃지 80×30이 우측 상단 모서리에 겹침) */
@Composable
private fun ExpiringWarrantyCard(
    item: ExpiringWarranty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.width(ExpiringCardWidth)) {
        Row(
            modifier = Modifier
                .padding(top = DdayOverhang)
                .size(ExpiringCardWidth, ExpiringCardHeight)
                .clip(Rounded2xl)
                .background(ColorBrandSenary)                 // 내부색 #F0F8FF
                .border(1.dp, ColorBrandPrimary, Rounded2xl)  // 테두리 1px #0088FF
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Thumbnail(url = item.thumbnailUrl, sizeDp = 84, bg = ColorBrandQuaternary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                LabelValueRow(stringResource(R.string.home_label_vendor), item.vendor)
                Spacer(Modifier.height(2.dp))
                LabelValueRow(stringResource(R.string.home_label_purchase), item.purchaseDate)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.home_label_warranty),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorBrandPrimary,
                        modifier = Modifier
                            .clip(RoundedLg)
                            .background(ColorBrandQuaternary)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.warrantyUntil,
                        fontSize = 13.sp,
                        color = ColorGray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        // D-day 뱃지 80×30 — 카드 우측 상단 모서리에 겹쳐 배치
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
                .size(DdayBadgeWidth, DdayBadgeHeight)
                .clip(RoundedFull)
                .background(ColorGray900),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.home_dday, item.dDay),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ColorWhite,
            )
        }
    }
}

/** 최근 등록 영수증 세로형 아이템 */
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
        color = ColorWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorGray100),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Thumbnail(url = item.thumbnailUrl, sizeDp = 48)
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
                    Text(text = stringResource(R.string.home_days_ago, item.daysAgo), fontSize = 12.sp, color = ColorGray400)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.home_label_purchase), fontSize = 13.sp, color = ColorGray500)
                    Text("  |  ", fontSize = 13.sp, color = ColorGray400)
                    Text(item.purchaseDate, fontSize = 13.sp, color = ColorGray500)
                }
            }
        }
    }
}

@Composable
private fun LabelValueRow(label: String, value: String) {
    Row {
        Text(text = label, fontSize = 13.sp, color = ColorGray500, modifier = Modifier.width(48.dp))
        Text(text = value, fontSize = 13.sp, color = ColorGray900)
    }
}

/** 제품 썸네일 — URL 있으면 이미지, 없으면 라이트블루 placeholder */
@Composable
private fun Thumbnail(url: String?, sizeDp: Int, bg: Color = ColorBrandSenary) {
    val m = Modifier
        .size(sizeDp.dp)
        .clip(RoundedXl)
        .background(bg)
    if (url != null) {
        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = m)
    } else {
        Box(modifier = m)
    }
}
