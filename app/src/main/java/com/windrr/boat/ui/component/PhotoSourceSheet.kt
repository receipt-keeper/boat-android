package com.windrr.boat.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Rounded2xl

private val RowHeight = 60.dp

/**
 * 사진 첨부 방법 선택 하단 액션 시트 (디자인 가이드).
 * "카메라로 촬영하기 / 갤러리에서 불러오기" 옵션 그룹 카드 + 별도 "닫기" 카드로 구성한다.
 * scrim(또는 카드 사이 여백) 탭 시 닫힌다.
 *
 * 홈 FAB용 [com.windrr.boat.feature.home.ReceiptAddSheet](FAB 위 팝업)와 달리,
 * 화면 하단 전체 폭 액션 시트가 필요한 영수증 입력/수정 화면에서 사용한다.
 */
@Composable
fun PhotoSourceSheet(
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
        // 카드 사이 여백/바깥 여백을 탭하면 scrim으로 전달되어 닫히도록 Column 자체에는 clickable을 두지 않는다.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 옵션 그룹 카드
            Surface(shape = Rounded2xl, color = ColorWhite) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ActionRow(
                        label = R.string.receipt_register_camera,
                        textColor = ColorBrandPrimary,
                        onClick = onCamera,
                    )
                    HorizontalDivider(thickness = 1.dp, color = ColorGray200)
                    ActionRow(
                        label = R.string.receipt_register_gallery,
                        textColor = ColorBrandPrimary,
                        onClick = onGallery,
                    )
                }
            }

            // 닫기 카드
            Surface(shape = Rounded2xl, color = ColorWhite) {
                ActionRow(
                    label = R.string.receipt_detail_menu_close,
                    textColor = ColorGray900,
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun ActionRow(
    @StringRes label: Int,
    textColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(RowHeight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(label),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}
