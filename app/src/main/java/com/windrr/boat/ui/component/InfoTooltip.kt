package com.windrr.boat.ui.component

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
import androidx.compose.ui.text.style.TextAlign
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
import com.windrr.boat.ui.theme.ColorBrandQuinary
import com.windrr.boat.ui.theme.ColorGray700
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.RoundedLg

/** 툴팁 말풍선 배경/포인터 색상 (#E6EBF4) */
private val TooltipBackground = ColorBrandQuinary

private val TriangleWidth = 14.dp
private val TriangleHeight = 7.dp

/** 정보 아이콘 — 클릭 시 아이콘 바로 위에 말풍선 툴팁을 띄운다. */
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
            // 말풍선이 화면 밖으로 나가 clamp 되더라도 삼각형 포인터는 항상 앵커(?) 정중앙을 가리켜야 한다.
            // → position provider가 계산한 "말풍선 좌측 끝 기준 앵커 중앙 x"를 상태로 받아 삼각형을 그 위치에 그린다.
            var pointerXInBubble by remember { mutableStateOf<Int?>(null) }
            val positionProvider = remember(gapPx) {
                TooltipPositionProvider(gapPx) { pointerX -> pointerXInBubble = pointerX }
            }
            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = { showTooltip = false },
                properties = PopupProperties(focusable = true),
            ) {
                TooltipBubble(text = tooltipText, pointerXPx = pointerXInBubble)
            }
        }
    }
}

/** #E6EBF4 말풍선 카드 + 하단 삼각 포인터(앵커 중앙을 가리킴) */
@Composable
private fun TooltipBubble(text: String, pointerXPx: Int?) {
    val density = LocalDensity.current
    val triangleHalfPx = with(density) { (TriangleWidth / 2).roundToPx() }

    Column(horizontalAlignment = Alignment.Start) {
        Surface(
            shape = RoundedLg,
            color = TooltipBackground,
            shadowElevation = 3.dp,
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .padding(horizontal = Margin12, vertical = 10.dp),
                fontSize = 12.sp,
                color = ColorGray700,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
            )
        }
        // pointerXPx: 말풍선 좌측 끝 기준 앵커 중앙 x. 아직 계산 전(첫 프레임)이면 중앙 정렬.
        val triangleModifier = if (pointerXPx != null) {
            Modifier.offset(x = with(density) { (pointerXPx - triangleHalfPx).toDp() })
        } else {
            Modifier.align(Alignment.CenterHorizontally)
        }
        Canvas(
            modifier = triangleModifier
                .size(width = TriangleWidth, height = TriangleHeight)
                .offset(y = (-1).dp),
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(path, color = TooltipBackground)
        }
    }
}

/**
 * 앵커(정보 아이콘) 바로 위, 가로로는 앵커 중앙에 맞춰 말풍선을 배치한다.
 * 화면 밖으로 나가면 좌우로 clamp 하고, 그때의 "말풍선 좌측 기준 앵커 중앙 x"를 [onPointerX]로 전달해
 * 삼각형 포인터가 앵커를 정확히 가리키도록 한다.
 */
private class TooltipPositionProvider(
    private val gapPx: Int,
    private val onPointerX: (Int) -> Unit,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val anchorCenterX = (anchorBounds.left + anchorBounds.right) / 2
        val rawX = anchorCenterX - popupContentSize.width / 2
        val clampedX = rawX.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))

        // 삼각형이 둥근 모서리를 벗어나지 않도록 좌우 안쪽으로 여백을 둔다.
        val edge = 14
        val pointerX = (anchorCenterX - clampedX)
            .coerceIn(edge, (popupContentSize.width - edge).coerceAtLeast(edge))
        onPointerX(pointerX)

        val y = anchorBounds.top - popupContentSize.height - gapPx
        return IntOffset(clampedX, y)
    }
}
