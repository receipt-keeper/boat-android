package com.windrr.boat.feature.terms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.Margin32
import com.windrr.boat.ui.theme.Margin8

/**
 * 마케팅 정보 수신 동의 상세 내용 화면.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingConsentDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "마케팅 정보 수신동의",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorGray900
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.common_back),
                            tint = Color.Unspecified
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorWhite
                )
            )
        },
        containerColor = ColorWhite
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Margin20)
                .padding(bottom = Margin32)
        ) {
            Spacer(Modifier.height(Margin12))

            Text(
                text = "[선택] 마케팅 정보 수신 동의",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900
            )

            Spacer(Modifier.height(Margin24))

            Text(
                text = "보트랩 서비스는 사용자에게 더 유용한 혜택과 서비스 소식을 전해드리기 위해 아래와 같이 마케팅 정보를 수집·활용하며, 이에 대해 선택적 동의를 받습니다.",
                fontSize = 14.sp,
                color = ColorGray600,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(Margin32))

            MarketingSectionTitle("1. 수집 및 활용 목적")
            Column(modifier = Modifier.padding(top = 4.dp)) {
                MarketingBulletText("맞춤형 서비스 안내: 서비스 기능 추천, 보증 관리 팁 및 신규 서비스 업데이트 소식 안내")
                MarketingBulletText("이벤트 및 프로모션: 각종 경품 행사, 제휴 이벤트 참여 안내 및 혜택 제공")
                MarketingBulletText("광고성 정보 전송: 앱 푸시 알림을 통한 광고성 정보 제공")
            }

            MarketingSectionTitle("2. 수집 항목")
            Text(
                text = "이메일 주소, 마케팅 수신 동의 여부, 수신 동의 일시 및 채널 정보",
                fontSize = 14.sp,
                color = ColorGray600,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            MarketingSectionTitle("3. 보유 및 이용 기간")
            Column(modifier = Modifier.padding(top = 4.dp)) {
                MarketingBulletText("회원 탈퇴 시까지 또는 이용자가 마케팅 정보 수신 동의를 철회하는 시점까지 보유하며, 철회 즉시 마케팅 활용 목적의 데이터는 파기됩니다.")
                MarketingBulletText("수신 동의 철회 이력은 관련 법령에 따라 부정 수신 방지 및 동의 여부 증빙을 위해 최대 1년간 보관될 수 있습니다.")
            }

            MarketingSectionTitle("4. 수신 동의 철회 및 설정 안내")
            Column(modifier = Modifier.padding(top = 4.dp)) {
                MarketingBulletText("이용자는 언제든지 [마이페이지] → [설정 알림]에서 광고성 푸시 알림 수신을 거부하거나 동의를 철회할 수 있으며, 철회 시 별도의 비용은 발생하지 않습니다.")
                MarketingBulletText("서비스 제공에 필수적인 안내(예: 보증 만료 알림 등)는 마케팅 수신 동의 여부와 관계없이 발송될 수 있습니다.")
            }

            MarketingSectionTitle("5. 동의 거부의 권리")
            Text(
                text = "이용자는 본 마케팅 정보 수신 동의를 거부할 권리가 있으며, 동의를 거부하더라도 보트랩 서비스의 기본 기능은 정상적으로 이용할 수 있습니다.",
                fontSize = 14.sp,
                color = ColorGray600,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun MarketingSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = ColorGray900,
        modifier = Modifier.padding(top = Margin24, bottom = Margin8)
    )
}

@Composable
private fun MarketingBulletText(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "• ",
            fontSize = 14.sp,
            color = ColorGray600,
            lineHeight = 22.sp
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = ColorGray600,
            lineHeight = 22.sp
        )
    }
}
