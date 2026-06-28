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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorGray200
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
                    Text(
                        stringResource(R.string.home_expiring_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorGray900
                    )
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
                    .background(ColorGray200) // #EEEEEE
                    .clickable(onClick = onRecentMore),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.home_more),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorGray600
                )
            }
        }
        Spacer(Modifier.height(Margin16))
    }
}

/** AS 만료 예정 가로형 카드 (334×183, D-day 뱃지 80×30이 우측 상단 모서리에 겹침) */
@Composable
private fun ExpiringWarrantyCard(
    item: ExpiringWarranty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 뱃지 높이 절반을 정확히 계산하여 카드 위쪽 여백 및 오프셋(Offset)에 사용
    val badgeHeight = 32.dp
    val badgeHalfHeight = badgeHeight / 2

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = badgeHalfHeight) // 뱃지가 튀어나올 상단 공간 확보
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 카드 그림자 — 옅은 블루 톤, 부드럽게
                .shadow(
                    elevation = 10.dp,
                    shape = Rounded2xl,
                    ambientColor = ColorBrandPrimary.copy(alpha = 0.10f),
                    spotColor = ColorBrandPrimary.copy(alpha = 0.12f),
                )
                .clip(Rounded2xl)
                .background(ColorBrandSenary)                 // 내부색 #F0F8FF
                .border(1.dp, ColorBrandPrimary, Rounded2xl)  // 테두리 1px #0088FF
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // 썸네일 — 흰 배경 박스 + 부드러운 그림자
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = ColorBrandPrimary.copy(alpha = 0.10f),
                        spotColor = ColorBrandPrimary.copy(alpha = 0.15f),
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorWhite),
                contentAlignment = Alignment.Center,
            ) {
                Thumbnail(url = item.thumbnailUrl, sizeDp = 84, bg = Color.Transparent)
            }

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
                Spacer(Modifier.height(12.dp))

                // 구매처 / 구매일 — 라벨·값 모두 #616161 통일
                LabelValueRow(stringResource(R.string.home_label_vendor), item.vendor)
                Spacer(Modifier.height(4.dp))
                LabelValueRow(stringResource(R.string.home_label_purchase), item.purchaseDate)

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 보증 기간 펠릿 — RoundedFull, 흰 배경, 파란 텍스트
                    Text(
                        text = stringResource(R.string.home_label_warranty), // "보증 기간"
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorBrandPrimary,
                        modifier = Modifier
                            .clip(RoundedFull)
                            .background(ColorWhite)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    // 만료일 — 강조 (크게·굵게·다크)
                    Text(
                        text = item.warrantyUntil,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorGray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // D-Day 뱃지 — Offset으로 카드 우측 상단 테두리에 정확히 걸침
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
                .offset(y = -badgeHalfHeight) // 수학적 중심점 정렬
                .height(badgeHeight)
                .clip(RoundedFull)
                .background(ColorGray900)
                .padding(horizontal = 16.dp), // 텍스트 길이에 맞춰 유동적 너비 확보
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.home_dday, item.dDay), // "D - 20"
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ColorWhite,
            )
        }
    }
}

/** 구매처/구매일 행 — 라벨·값 모두 #616161, 라벨 고정폭으로 값 시작점 정렬 */
@Composable
private fun LabelValueRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ColorLabel,
            modifier = Modifier.width(48.dp), // "Apple"과 "2025.03.13"의 시작점을 동일하게 맞추기 위한 고정폭
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ColorLabel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
                    Text(
                        text = stringResource(R.string.home_days_ago, item.daysAgo),
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

private val ColorLabel = Color(0xFF616161)

/** 제품 썸네일 — URL 있으면 이미지, 없으면 라이트블루 placeholder */
@Composable
private fun Thumbnail(url: String?, sizeDp: Int, bg: Color = ColorBrandSenary) {
    val m = Modifier
        .size(sizeDp.dp)
        .clip(RoundedXl)
        .background(bg)
    if (url != null) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = m
        )
    } else {
        Box(modifier = m)
    }
}
