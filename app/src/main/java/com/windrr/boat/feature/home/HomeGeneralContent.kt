package com.windrr.boat.feature.home

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.platform.LocalConfiguration
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

/** AS 만료 예정 가로형 카드 — 화면폭 기준 너비, D-day 뱃지가 우측 상단 테두리에 겹침 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun ExpiringWarrantyCard(
    item: ExpiringWarranty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val badgeHeight = 32.dp
    val badgeHalfHeight = badgeHeight / 2
    val cardWidth = LocalConfiguration.current.screenWidthDp.dp - 52.dp

    Box(
        modifier = modifier
            .width(cardWidth)
            .padding(top = badgeHalfHeight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // [교정 1] 탁한 그림자 완전 제거. 플랫 디자인의 1px 테두리만 유지.
                .clip(Rounded2xl)
                .background(ColorBrandSenary)
                .border(1.dp, ColorBrandPrimary, Rounded2xl)
                .clickable(onClick = onClick)
                .padding(20.dp), // 스크린샷의 넉넉한 내부 여백 반영
            verticalAlignment = Alignment.Top,
        ) {
            // [교정 2] 썸네일 크기 축소 (100dp -> 88dp) 및 그림자 제거
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorWhite),
                contentAlignment = Alignment.Center,
            ) {
                Thumbnail(url = item.thumbnailUrl, sizeDp = 88, bg = Color.Transparent)
            }

            Spacer(Modifier.width(16.dp))

            // [교정 3] 강제 높이(heightIn) 및 SpaceBetween 제거. 콘텐츠에 맞춰 자연스럽게 흐르도록 수정
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 18.sp, // 20sp에서 스탠다드 규격으로 축소
                    fontWeight = FontWeight.Bold,
                    color = ColorGray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(14.dp)) // 타이틀과 정보 사이의 명확한 여백

                // 라벨 그룹은 간격을 타이트하게 묶음
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LabelValueRow(stringResource(R.string.home_label_vendor), item.vendor)
                    LabelValueRow(stringResource(R.string.home_label_purchase), item.purchaseDate)
                }

                Spacer(Modifier.height(16.dp)) // 보증기간 뱃지와 분리

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.home_label_warranty),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorBrandPrimary,
                        modifier = Modifier
                            .clip(RoundedFull)
                            .background(ColorWhite)
                            .padding(horizontal = 8.dp, vertical = 4.dp), // 내부 패딩 타이트하게 조절
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.warrantyUntil,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium, // SemiBold 제거. 스크린샷처럼 담백하게 유지
                        color = ColorGray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // D-Day 뱃지
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                // 스크린샷 비율상 우측 끝에서 살짝 더 들어와 있음
                .padding(end = 24.dp)
                .offset(y = -badgeHalfHeight)
                .height(badgeHeight)
                .clip(RoundedFull)
                .background(ColorGray900)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.home_dday, item.dDay),
                fontSize = 14.sp,
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
