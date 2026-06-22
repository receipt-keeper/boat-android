package com.windrr.boat.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20

private val HeaderHeight = 56.dp

/**
 * 공통 헤더 — 좌측 로고, 우측 검색/알림 아이콘.
 * 여러 화면에서 재사용한다. (로고는 추후 이미지 에셋으로 교체 예정)
 */
@Composable
fun BoatHeader(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .padding(horizontal = Margin20),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.header_logo),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorGray900,
        )
        Spacer(Modifier.weight(1f))
        HeaderActionIcon(
            icon = R.drawable.ic_search,
            description = R.string.header_search,
            onClick = onSearchClick,
        )
        Spacer(Modifier.width(Margin16))
        HeaderActionIcon(
            icon = R.drawable.ic_notification,
            description = R.string.header_notification,
            onClick = onNotificationClick,
        )
    }
}

@Composable
private fun HeaderActionIcon(
    @DrawableRes icon: Int,
    @StringRes description: Int,
    onClick: () -> Unit,
) {
    val noRipple = remember { MutableInteractionSource() }
    // 에셋 고유 크기(검색 20×20, 알림 18×20)로 렌더 — 정사각형 강제 금지(비율 왜곡 방지)
    Icon(
        painter = painterResource(icon),
        contentDescription = stringResource(description),
        tint = ColorGray900,
        modifier = Modifier
            .clickable(interactionSource = noRipple, indication = null, onClick = onClick),
    )
}
