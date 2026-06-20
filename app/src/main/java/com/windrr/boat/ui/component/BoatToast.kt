package com.windrr.boat.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorSystemError
import com.windrr.boat.ui.theme.ColorSystemToast
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.RoundedLg
import kotlinx.coroutines.delay

// ── 타입 ──────────────────────────────────────────────────────────────────────

enum class BoatToastType { ERROR, SUCCESS, INFO }

// ── 상태 ──────────────────────────────────────────────────────────────────────

class BoatToastState {
    var current by mutableStateOf<Pair<String, BoatToastType>?>(null)
        private set

    fun show(message: String, type: BoatToastType = BoatToastType.INFO) {
        current = message to type
    }

    fun showError(message: String) = show(message, BoatToastType.ERROR)
    fun showSuccess(message: String) = show(message, BoatToastType.SUCCESS)

    fun dismiss() { current = null }
}

@Composable
fun rememberBoatToastState(): BoatToastState = remember { BoatToastState() }


@Composable
fun BoatToastHost(
    state: BoatToastState,
    topOffsetDp: Int = 56
) {
    val current = state.current

    LaunchedEffect(current) {
        if (current != null) {
            delay(3_000L)
            state.dismiss()
        }
    }

    // exit 애니메이션 중에도 콘텐츠를 유지하기 위해 마지막 데이터를 snapshot으로 보존
    var snapshot by remember { mutableStateOf(current) }
    if (current != null) snapshot = current

    val topOffsetPx = with(LocalDensity.current) { topOffsetDp.dp.roundToPx() }

    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(x = 0, y = topOffsetPx),
        properties = PopupProperties(focusable = false)
    ) {
        AnimatedVisibility(
            visible = current != null,
            enter = fadeIn() + slideInVertically { -it },
            exit  = fadeOut() + slideOutVertically { -it }
        ) {
            snapshot?.let { (message, type) ->
                Box(modifier = Modifier.padding(horizontal = Margin20)) {
                    BoatToastItem(message = message, type = type)
                }
            }
        }
    }
}

@Composable
private fun BoatToastItem(
    message: String,
    type: BoatToastType,
    modifier: Modifier = Modifier
) {
    val isError = type == BoatToastType.ERROR
    val iconRes = if (isError) R.drawable.toast_red else R.drawable.toast_info
    // 아이콘 배경: FE395B @10% / 0088FF @10%
    val bgColor = if (isError) Color(0x1AFE395B) else Color(0x1A0088FF)
    val tint    = if (isError) ColorSystemError else ColorBrandPrimary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(ColorSystemToast, RoundedLg)
            .padding(horizontal = Margin16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 연한 배경 원(32dp) + 아이콘(18dp)
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(Margin12))

        Text(
            text = message,
            color = ColorGray50,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}
