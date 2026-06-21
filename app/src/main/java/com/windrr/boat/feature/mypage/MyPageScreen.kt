package com.windrr.boat.feature.mypage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.component.BoatDialog
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorSystemError

/**
 * 마이 탭 — 임시 placeholder. 로그아웃 / 회원 탈퇴 진입점.
 */
@Composable
fun MyPageScreen(
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.tab_my),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ColorGray900,
        )
        Spacer(Modifier.height(32.dp))
        OutlinedButton(onClick = onSignOut) {
            Text(stringResource(R.string.common_logout))
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { showDeleteDialog = true }) {
            Text(
                text = stringResource(R.string.account_delete),
                color = ColorSystemError,
            )
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
