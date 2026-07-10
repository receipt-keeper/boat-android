package com.windrr.boat.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private val ShimmerBase = Color(0xFFE9EEF6)      // 옅은 쿨블루그레이 (플레이스홀더 기본)
private val ShimmerHighlight = Color(0xFFF3F7FC) // 하이라이트(빛 스쳐가는 밴드)

/**
 * 로딩 스켈레톤용 셔머 브러시. 화면당 한 번 생성해 여러 플레이스홀더에 공유하면
 * 빛 밴드가 좌→우로 함께 쓸고 지나가는 효과를 낸다.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerX",
    )
    return Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        start = Offset(x - 400f, 0f),
        end = Offset(x, 0f),
    )
}

/** 셔머 배경 + 라운드 처리를 한 번에 적용하는 스켈레톤 플레이스홀더 modifier. */
fun Modifier.shimmer(brush: Brush, shape: Shape = RoundedCornerShape(6.dp)): Modifier =
    this.background(brush, shape)
