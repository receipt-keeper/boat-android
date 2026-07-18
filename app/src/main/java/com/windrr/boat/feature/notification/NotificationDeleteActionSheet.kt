package com.windrr.boat.feature.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorSystemError
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Rounded2xl

/**
 * 알림 카드 케밥 → "삭제하기" 확인 시트. 디자인 가이드: 타이틀 없이
 * 화면 하단에 삭제(빨강)/닫기 두 버튼만 카드형으로 노출.
 */
@Composable
fun NotificationDeleteActionSheet(
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionSheetButton(text = "삭제하기", textColor = ColorSystemError, onClick = onDelete)
                ActionSheetButton(text = "닫기", textColor = ColorGray900, onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun ActionSheetButton(text: String, textColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Rounded2xl)
            .background(ColorWhite)
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}
