package com.windrr.boat.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorSystemError
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20

private val HeaderHeight = 56.dp

/**
 * 공통 헤더 — 좌측 로고(또는 뒤로가기), 우측 검색/알림 아이콘.
 * 여러 화면에서 재사용한다. (로고는 추후 이미지 에셋으로 교체 예정)
 * [onBackClick]이 주어지면 좌측이 로고/타이틀 대신 뒤로가기 아이콘으로 대체된다(서브 화면용).
 */
@Composable
fun BoatHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    hasUnreadNotification: Boolean = false,
    titleColor: Color = ColorGray900,
    iconTint: Color = ColorGray900,
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .padding(horizontal = Margin20),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackClick != null) {
            HeaderActionIcon(
                icon = R.drawable.ic_arrow_back,
                description = R.string.common_back,
                tint = iconTint,
                onClick = onBackClick,
            )
        } else if (title != null) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )
        } else {
            // 앱 로고 — "Boat"(검정) + "Lab"(파란색)
            Text(text = "Boat ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Text(text = "Lab",   fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (titleColor == ColorWhite) ColorWhite else ColorBrandPrimary)
        }
        Spacer(Modifier.weight(1f))
        HeaderActionIcon(
            icon = R.drawable.ic_search,
            description = R.string.header_search,
            tint = iconTint,
            onClick = onSearchClick,
        )
        Spacer(Modifier.width(Margin16))
        HeaderActionIcon(
            icon = R.drawable.ic_notification,
            description = R.string.header_notification,
            showBadge = hasUnreadNotification,
            tint = iconTint,
            onClick = onNotificationClick,
        )
    }
}

@Composable
private fun HeaderActionIcon(
    @DrawableRes icon: Int,
    @StringRes description: Int,
    showBadge: Boolean = false,
    tint: Color = ColorGray900,
    onClick: () -> Unit,
) {
    val noRipple = remember { MutableInteractionSource() }
    Box {
        // 에셋 고유 크기(검색 20×20, 알림 18×20)로 렌더 — 정사각형 강제 금지(비율 왜곡 방지)
        Icon(
            painter = painterResource(icon),
            contentDescription = stringResource(description),
            tint = tint,
            modifier = Modifier
                .clickable(interactionSource = noRipple, indication = null, onClick = onClick),
        )
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(ColorWhite)
                    .padding(1.dp)
                    .clip(CircleShape)
                    .background(ColorSystemError),
            )
        }
    }
}
