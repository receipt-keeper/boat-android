package com.windrr.boat.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray700
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.RoundedLg

/** 정보 아이콘 — 클릭 시 아이콘 위에 말풍선 툴팁을 띄운다. */
@Composable
fun InfoTooltipIcon(tooltipText: String) {
    var showTooltip by remember { mutableStateOf(false) }
    val gapPx = with(LocalDensity.current) { 6.dp.roundToPx() }

    Box(
        modifier = Modifier
            .size(18.dp)
            .clickable(onClick = { showTooltip = true }),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.info_question_icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(17.dp),
        )

        if (showTooltip) {
            Popup(
                popupPositionProvider = remember(gapPx) { TooltipPositionProvider(gapPx) },
                onDismissRequest = { showTooltip = false },
                properties = PopupProperties(focusable = true),
            ) {
                TooltipBubble(text = tooltipText)
            }
        }
    }
}

/** 흰 말풍선 카드 + 하단 삼각 포인터 */
@Composable
private fun TooltipBubble(text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedLg,
            color = ColorWhite,
            border = BorderStroke(1.dp, ColorGray200),
            shadowElevation = 4.dp,
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .padding(horizontal = Margin12, vertical = 10.dp),
                fontSize = 12.sp,
                color = ColorGray700,
                lineHeight = 17.sp,
            )
        }
        Canvas(
            modifier = Modifier
                .size(width = 14.dp, height = 7.dp)
                .offset(y = (-1).dp),
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(path, color = ColorWhite)
        }
    }
}

/** 앵커(정보 아이콘) 바로 위, 가로 중앙에 툴팁을 배치한다. */
private class TooltipPositionProvider(private val gapPx: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = (anchorBounds.left + anchorBounds.right) / 2 - popupContentSize.width / 2
        val clampedX = x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val y = anchorBounds.top - popupContentSize.height - gapPx
        return IntOffset(clampedX, y)
    }
}
