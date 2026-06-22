package com.windrr.boat.feature.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Rounded2xl

// 카드 크기/아이콘 (디자인 가이드)
private val MenuWidth = 214.dp
private val MenuItemHeight = 76.dp
private val MenuIconSize = 28.dp

// 카드를 FAB 위로 띄우는 하단 여백 (BottomBar 80 + FAB margin 16 + FAB 56 + gap 12 ≈ 164dp)
private val MenuBottomOffset = 164.dp
// 카드 오른쪽 변을 FAB 중심에 맞추는 오른쪽 여백 (FAB end margin 16 + FAB 반지름 28 = 44dp)
private val MenuEndOffset = 44.dp

/**
 * 영수증 등록 FAB 메뉴 오버레이.
 * scrim(탭 시 닫힘) 위에, 카드의 오른쪽 변이 FAB 중심에 오도록 FAB 위쪽으로 띄운다.
 * 카드: 214dp × (76dp × 2) — "사진으로 찍기 / 갤러리에서 불러오기"
 */
@Composable
fun ReceiptAddSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
) {
    val noRipple = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            // scrim 영역 탭 → 닫기
            .clickable(interactionSource = noRipple, indication = null) { onDismiss() },
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = MenuEndOffset, bottom = MenuBottomOffset)
                .width(MenuWidth)
                // 카드 영역 탭은 scrim으로 전파되지 않도록 소비(no-op)
                .clickable(interactionSource = noRipple, indication = null) {},
            shape = Rounded2xl,
            color = ColorWhite,
            shadowElevation = 12.dp,
        ) {
            Column {
                AddMenuItem(R.drawable.ic_camera, R.string.receipt_add_camera, onCamera)
                AddMenuItem(R.drawable.ic_gallery, R.string.receipt_add_gallery, onGallery)
            }
        }
    }
}

@Composable
private fun AddMenuItem(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MenuItemHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = Margin20),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = ColorGray900,
            modifier = Modifier.size(MenuIconSize),
        )
        Spacer(Modifier.width(Margin16))
        Text(
            text = stringResource(label),
            fontSize = 16.sp,
            color = ColorGray900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
