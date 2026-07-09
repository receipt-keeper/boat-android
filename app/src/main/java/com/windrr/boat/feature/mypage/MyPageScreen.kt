package com.windrr.boat.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.R
import com.windrr.boat.feature.notification.NotificationBadgeViewModel
import com.windrr.boat.feature.receipt.ReceiptRegisterActivity
import com.windrr.boat.ui.component.BoatDialog
import com.windrr.boat.ui.component.BoatHeader
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.BottomBarClearance
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorBrandTertiary
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray800
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.RoundedFull
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 마이 탭 — 공통 헤더 + 프로필 + 영수증 분석 잔여 배너 + 설정/도움말 메뉴 + 로그아웃/회원탈퇴.
 * 이름·이메일이 없으면 플레이스홀더("이름", "email@naver.com")로 표시.
 */
@Composable
fun MyPageScreen(
    name: String?,
    email: String?,
    freeAnalysisTokens: Int,
    profileImageUrl: String? = null,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    badgeViewModel: NotificationBadgeViewModel = viewModel(),
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAnalysisPromoSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val toastState = rememberBoatToastState()
    val hasUnreadNotification by badgeViewModel.hasUnread.collectAsState()

    LaunchedEffect(Unit) { badgeViewModel.refresh() }

    val nameText = name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.mypage_name_placeholder)
    val emailText = email?.takeIf { it.isNotBlank() } ?: stringResource(R.string.mypage_email_placeholder)

    val inquiryEmail = stringResource(R.string.mypage_inquiry_email)
    val inquirySubject = stringResource(R.string.mypage_inquiry_subject)
    val noEmailAppMsg = stringResource(R.string.mypage_inquiry_no_app)

    // 1:1 문의 — 메일 앱으로 작성 화면 열기
    fun openInquiryEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(inquiryEmail))
            putExtra(Intent.EXTRA_SUBJECT, inquirySubject)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { toastState.showError(noEmailAppMsg) }
    }

    Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
        BoatHeader(
            title = stringResource(R.string.mypage_title),
            hasUnreadNotification = hasUnreadNotification,
            onSearchClick = onSearchClick,
            onNotificationClick = {
                context.startActivity(
                    Intent(context, com.windrr.boat.feature.notification.NotificationListActivity::class.java)
                )
            },
        )

        // 프로필
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val avatarModifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ColorBrandSenary)
            if (!profileImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = avatarModifier,
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.img_profile),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = avatarModifier,
                )
            }
            Spacer(Modifier.width(Margin16))
            Column {
                Text(text = nameText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorGray900)
                Spacer(Modifier.height(4.dp))
                Text(text = emailText, fontSize = 14.sp, color = ColorGray800)
            }
        }

        // 영수증 분석 잔여 횟수 배너
        AnalysisCreditBanner(
            remaining = freeAnalysisTokens,
            onViewClick = { showAnalysisPromoSheet = true },
            modifier = Modifier.padding(horizontal = Margin20),
        )
        Spacer(Modifier.height(Margin20))

        // 섹션 구분 밴드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(com.windrr.boat.ui.theme.ColorGray50),
        )

        // 알림 설정
        SectionLabel(stringResource(R.string.mypage_section_settings))
        SettingRow(stringResource(R.string.mypage_section_notification)) {
            context.startActivity(
                Intent(context, com.windrr.boat.feature.notification.NotificationSettingsActivity::class.java)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Margin20),
            thickness = 1.dp,
            color = ColorGray200,
        )

        // 도움말
        SectionLabel(stringResource(R.string.mypage_section_help))
        SettingRow(stringResource(R.string.mypage_inquiry)) { openInquiryEmail() }
        SettingRow(stringResource(R.string.mypage_terms)) { /* TODO: 서비스 이용약관 */ }

        Spacer(Modifier.weight(1f))

        // 로그아웃 | 회원탈퇴 — 플로팅 하단 바에 가려지지 않도록 여백 확보
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = BottomBarClearance),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomTextButton(stringResource(R.string.common_logout), onClick = { showLogoutDialog = true })
            Text(text = "  |  ", fontSize = 14.sp, color = ColorGray200)
            BottomTextButton(stringResource(R.string.mypage_withdraw), onClick = { showDeleteDialog = true })
        }
    }

        BoatToastHost(state = toastState)
    }

    // 로그아웃 확인 다이얼로그
    if (showLogoutDialog) {
        BoatDialog(
            title = stringResource(R.string.account_logout_dialog_title),
            message = stringResource(R.string.account_logout_dialog_message),
            confirmText = stringResource(R.string.common_logout),
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = {
                showLogoutDialog = false
                onSignOut()
            },
            onDismiss = { showLogoutDialog = false },
        )
    }

    // 회원 탈퇴 확인 다이얼로그
    if (showDeleteDialog) {
        BoatDialog(
            title = stringResource(R.string.account_delete_dialog_title),
            message = stringResource(R.string.account_delete_dialog_message),
            confirmText = stringResource(R.string.account_delete_confirm),
            dismissText = stringResource(R.string.account_delete_dialog_dismiss),
            onConfirm = {
                showDeleteDialog = false
                onDeleteAccount()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    // 영수증 분석 이벤트 안내 BottomSheet — "지금 영수증 등록하기"는 등록 화면으로 이동
    if (showAnalysisPromoSheet) {
        AnalysisPromoBottomSheet(
            onDismiss = { showAnalysisPromoSheet = false },
            onRegisterClick = {
                showAnalysisPromoSheet = false
                context.startActivity(Intent(context, ReceiptRegisterActivity::class.java))
            },
        )
    }
}

/** "영수증 분석 N회 남음" 배너 — 좌측 스파클 + 문구, 우측 "보기" 버튼(파란 pill). */
@Composable
private fun AnalysisCreditBanner(
    remaining: Int,
    onViewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedXl)
            .background(ColorBrandSenary)
            .border(1.dp, ColorBrandTertiary, RoundedXl)
            .padding(horizontal = Margin16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ai_color),
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.Unspecified,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(text = stringResource(R.string.mypage_analysis_label), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ColorGray900)
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.mypage_analysis_count, remaining),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ColorBrandPrimary,
        )
        Spacer(Modifier.width(4.dp))
        Text(text = stringResource(R.string.mypage_analysis_suffix), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ColorGray900)

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .clip(RoundedFull)
                .background(ColorBrandPrimary)
                .clickable(onClick = onViewClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.mypage_analysis_view),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorWhite,
            )
        }
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
