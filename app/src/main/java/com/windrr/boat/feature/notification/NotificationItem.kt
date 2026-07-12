package com.windrr.boat.feature.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.core.ocr.DeviceImage
import com.windrr.boat.ui.theme.ColorGray100
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Rounded2xl

/** 상시 유도 알림(registration_prompt/marketing)의 고정 수신거부 안내 문구 — 데이터와 무관하게 항상 동일하게 노출. */
private const val PERSISTENT_NOTIFICATION_FOOTER =
    "([Boatlab] 수신거부: 설정 내 알림 메뉴에서 변경 가능)"

/**
 * 알림 목록 아이템 (재사용 컴포넌트) — 디자인 가이드 스펙.
 *
 * 카테고리(보증/혜택 등)와 무관하게 상단 라벨은 항상 "보트랩" 고정 → 날짜 →
 * 타이틀(제품명) → 메시지 순 세로 배치. 상시 유도 알림(registration_prompt/marketing)만
 * 최하단에 고정 수신거부 문구가 추가로 붙는다. 썸네일은 항상 상단 라인에 맞춰 정렬한다.
 */
@Composable
fun NotificationItem(
    notification: AppNotification,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val alpha = if (notification.isRead) 0.5f else 1f
    val isPersistent = notification.kind == "registration_prompt" || notification.messageType == "marketing"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Rounded2xl,
        color = ColorWhite,
        shadowElevation = if (notification.isRead) 0.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .drawWithContent {
                    drawContent()
                    if (notification.isRead) {
                        drawRect(color = Color.White.copy(alpha = 0.3f))
                    }
                },
            verticalAlignment = Alignment.Top,
        ) {
            // 썸네일 — 카테고리/기기 이미지(metadata.subCategory 기반)
            // 💡 상시유도알림(registration_prompt)이나 마케팅형 알림은 항상 '기타' 대분류 이미지를 노출
            val imageRes = if (isPersistent) {
                DeviceImage.categoryDefault("기타 제품")
            } else {
                DeviceImage.resolve(null, notification.subCategory)
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ColorGray100.copy(alpha = alpha)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    alpha = alpha
                )
            }

            Spacer(Modifier.width(14.dp))

            // 카테고리 라벨(보증/혜택 등)과 무관하게 상단 라벨은 항상 "보트랩" 고정.
            // 상시 유도 알림만 최하단에 고정 수신거부 문구가 추가로 붙는다.
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "보트랩",
                        fontSize = 14.sp,
                        color = ColorGray600.copy(alpha = alpha),
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = notification.date,
                        fontSize = 14.sp,
                        color = ColorGray500.copy(alpha = alpha),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = notification.productName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorGray900.copy(alpha = alpha),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = ColorGray600.copy(alpha = alpha),
                )
                if (isPersistent) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = PERSISTENT_NOTIFICATION_FOOTER,
                        fontSize = 12.sp,
                        color = ColorGray400.copy(alpha = alpha),
                    )
                }
            }
        }
    }
}
