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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin32
import com.windrr.boat.ui.theme.Rounded2xl

/**
 * 영수증 등록 FAB 메뉴 오버레이.
 * 화면 전체를 덮는 scrim(탭 시 닫힘) 위에 "사진으로 찍기 / 갤러리에서 불러오기" 카드를 띄운다.
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
                .align(Alignment.BottomCenter)
                .padding(horizontal = Margin32)
                .padding(bottom = 140.dp)
                // 카드 영역 탭은 scrim으로 전파되지 않도록 소비(no-op)
                .clickable(interactionSource = noRipple, indication = null) {},
            shape = Rounded2xl,
            color = ColorWhite,
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.padding(vertical = Margin8)) {
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
            .clickable(onClick = onClick)
            .padding(horizontal = Margin20, vertical = Margin16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = ColorGray900,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(Margin16))
        Text(
            text = stringResource(label),
            fontSize = 16.sp,
            color = ColorGray900,
        )
    }
}
