package com.windrr.boat.feature.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.component.BoatDialog
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray800
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorSystemError
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20

/**
 * 마이 탭 — 공통 헤더 + 프로필 + 설정/도움말 메뉴 + 로그아웃/회원탈퇴.
 * 이름·이메일이 없으면 플레이스홀더("이름", "email@naver.com")로 표시.
 */
@Composable
fun MyPageScreen(
    name: String?,
    email: String?,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val nameText = name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.mypage_name_placeholder)
    val emailText = email?.takeIf { it.isNotBlank() } ?: stringResource(R.string.mypage_email_placeholder)

    Column(modifier = modifier.fillMaxSize()) {
        BoatHeader(
            title = stringResource(R.string.mypage_title),
            onSearchClick = { /* TODO: 검색 */ },
            onNotificationClick = { /* TODO: 알림 */ },
        )

        // 프로필
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ColorBrandSenary),
            )
            Spacer(Modifier.width(Margin16))
            Column {
                Text(text = nameText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorGray900)
                Spacer(Modifier.height(4.dp))
                Text(text = emailText, fontSize = 14.sp, color = ColorGray800)
            }
        }

        // 섹션 구분 밴드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(com.windrr.boat.ui.theme.ColorGray50),
        )

        // 알림 설정
        SectionLabel(stringResource(R.string.mypage_section_notification))
        SettingRow(stringResource(R.string.mypage_section_notification)) { /* TODO: 알림 설정 */ }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Margin20),
            thickness = 1.dp,
            color = ColorGray200,
        )

        // 도움말
        SectionLabel(stringResource(R.string.mypage_section_help))
        SettingRow(stringResource(R.string.mypage_inquiry)) { /* TODO: 1:1 문의하기 */ }
        SettingRow(stringResource(R.string.mypage_terms)) { /* TODO: 서비스 이용약관 */ }

        Spacer(Modifier.weight(1f))

        // 로그아웃 | 회원탈퇴
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomTextButton(stringResource(R.string.common_logout), onClick = onSignOut)
            Text(text = "  |  ", fontSize = 14.sp, color = ColorGray200)
            BottomTextButton(stringResource(R.string.mypage_withdraw), onClick = { showDeleteDialog = true })
        }
    }

    if (showDeleteDialog) {
        BoatDialog(
            title = stringResource(R.string.account_delete_dialog_title),
            message = stringResource(R.string.account_delete_dialog_message),
            confirmText = stringResource(R.string.account_delete_confirm),
            confirmTextColor = ColorSystemError,
            onConfirm = {
                showDeleteDialog = false
                onDeleteAccount()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = ColorGray500,
        modifier = Modifier.padding(start = Margin20, end = Margin20, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Margin20, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, fontSize = 16.sp, color = ColorGray900, modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.Unspecified,
        )
    }
}

@Composable
private fun BottomTextButton(text: String, onClick: () -> Unit) {
    val noRipple = remember { MutableInteractionSource() }
    Text(
        text = text,
        fontSize = 14.sp,
        color = ColorGray500,
        modifier = Modifier.clickable(interactionSource = noRipple, indication = null, onClick = onClick),
    )
}
