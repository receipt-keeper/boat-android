package com.windrr.boat.feature.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.font.FontWeight
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

// 카드를 FAB 위로 띄우는 하단 여백
// BoatBottomBar의 vertical margin 12dp + BarHeight 62dp + gap 12dp = 86dp
private val MenuBottomOffset = 86.dp

// 카드 오른쪽 변을 FAB 중심에 맞추는 오른쪽 여백
// BoatBottomBar의 horizontal margin 20dp + FabSize 62dp/2 = 51dp
private val MenuEndOffset = 20.dp

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
            .clickable(interactionSource = noRipple, indication = null) { onDismiss() },
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = MenuEndOffset, bottom = MenuBottomOffset)
                // [교정 1] 고정폭(MenuWidth) 삭제. 컨테이너가 내부 컨텐츠 길이에 맞춰지도록 허용
                .wrapContentWidth()
                .widthIn(min = 200.dp) // 단, 너무 좁아지는 것을 막기 위한 최소 너비만 보장
                .clickable(interactionSource = noRipple, indication = null) {},
            shape = Rounded2xl,
            color = ColorWhite,
            shadowElevation = 12.dp,
        ) {
            // [교정 2] 자식(Row)들 중 가장 긴 텍스트의 길이에 맞춰 Column 너비를 동기화
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp) // 메뉴 간격 타이트하게 조절
            ) {
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
            .fillMaxWidth() // Column의 IntrinsicSize.Max에 맞춰 꽉 차게 확장됨
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.Unspecified,
            // [교정 3] 아이콘 크기를 OS 스탠다드 규격인 24dp로 롤백
            modifier = Modifier.size(24.dp),
        )
        // [교정 4] 아이콘과 텍스트 사이 간격을 스크린샷과 동일한 텐션(12dp)으로 축소
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(label),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            // 이제 공간이 부족해서 잘릴 일이 없으므로 maxLines 설정이 온전히 방어용으로만 작동함
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}