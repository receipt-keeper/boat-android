package com.windrr.boat.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900

/**
 * 홈 탭 — 공통 헤더 + 임시 placeholder (디자인 확정 후 영수증 등록/추천 카드 등으로 발전 예정)
 */
@Composable
fun HomeScreen(
    displayName: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BoatHeader(
            onSearchClick = { /* TODO: 검색 */ },
            onNotificationClick = { /* TODO: 알림 */ },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.tab_home),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
            )
            displayName?.let {
                Spacer(Modifier.height(8.dp))
                Text(text = it, fontSize = 16.sp, color = ColorGray500)
            }
        }
    }
}
