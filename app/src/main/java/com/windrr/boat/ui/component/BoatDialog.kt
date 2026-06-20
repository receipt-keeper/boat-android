package com.windrr.boat.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Rounded2xl

@Composable
fun BoatDialog(
    message: String,
    onDismiss: () -> Unit,
    // 제목 (없으면 숨김)
    title: String? = null,
    // 확인 버튼
    confirmText: String? = null,
    confirmTextColor: Color = ColorBrandPrimary,
    onConfirm: () -> Unit = onDismiss,
    // 취소 버튼 (없으면 숨김)
    dismissText: String? = null,
    dismissTextColor: Color = ColorGray600,
    showDismissButton: Boolean = true,
    // 스타일
    messageStyle: TextStyle? = null,
    dismissOnClickOutside: Boolean = true,
) {
    val resolvedConfirmText = confirmText ?: stringResource(R.string.common_confirm)
    val resolvedDismissText = dismissText ?: stringResource(R.string.common_cancel)

    AlertDialog(
        onDismissRequest = { if (dismissOnClickOutside) onDismiss() },
        containerColor = ColorWhite,
        shape = Rounded2xl,
        title = title?.let {
            {
                Text(
                    text = it,
                    color = ColorGray900,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        text = {
            Text(
                text = message,
                color = ColorGray900,
                style = messageStyle ?: MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = resolvedConfirmText,
                    color = confirmTextColor,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        dismissButton = if (showDismissButton) {
            {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = resolvedDismissText,
                        color = dismissTextColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        } else null,
    )
}
