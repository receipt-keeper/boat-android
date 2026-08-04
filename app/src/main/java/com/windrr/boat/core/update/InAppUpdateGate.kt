package com.windrr.boat.core.update

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.windrr.boat.ui.component.BoatDialog

/**
 * Google Play In-App Updates(Flexible) — 앱 진입 시 스토어에 새 버전이 있으면 백그라운드로
 * 다운로드를 시작하고, 다운로드가 끝나면 재시작을 유도하는 다이얼로그를 띄운다.
 *
 * - Flexible 방식이라 다운로드 중에도 앱을 계속 쓸 수 있다(Immediate처럼 화면을 막지 않음).
 * - 실제 동작 확인은 Play Console 내부 테스트 트랙 이상에 배포된 빌드를 그 트랙으로
 *   설치한 기기에서만 가능하다 — 로컬 디버그/사이드로드 설치본은 항상 "업데이트 없음"이다.
 *
 * 화면 자체는 렌더링하지 않고 효과 + (다운로드 완료 시) 재시작 다이얼로그만 띄운다.
 * NotificationPermissionGate와 동일한 패턴 — 로그인 후 화면(HomeActivity)에 배치한다.
 */
@Composable
fun InAppUpdateGate() {
    val activity = LocalActivity.current ?: return
    val appUpdateManager = remember { AppUpdateManagerFactory.create(activity) }

    var showRestartDialog by remember { mutableStateOf(false) }

    // Play가 업데이트 동의 화면을 보여주는 런처. 사용자가 취소해도 별도 처리는 하지 않는다 —
    // 다음 앱 진입 시 LaunchedEffect가 다시 물어본다.
    val updateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { }

    // 다운로드 완료 감지. registerListener는 등록된 "이후"의 상태 변화만 콜백하므로,
    // 등록 시점 이전에 이미 완료돼 있던 경우는 아래 LaunchedEffect의 최초 조회에서 잡아낸다.
    DisposableEffect(appUpdateManager) {
        val listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                showRestartDialog = true
            }
        }
        appUpdateManager.registerListener(listener)
        onDispose { appUpdateManager.unregisterListener(listener) }
    }

    // 앱 진입(콜드 스타트/화면 재진입)마다 확인 — Google 권장: "모든 앱 진입 지점에서 체크".
    LaunchedEffect(Unit) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                // 지난번 다운로드는 끝났지만 사용자가 재시작을 미룬 채 앱을 껐다가 다시 연 경우.
                info.installStatus() == InstallStatus.DOWNLOADED -> showRestartDialog = true

                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                    )
                }
            }
        }
    }

    if (showRestartDialog) {
        BoatDialog(
            title = "새 버전이 준비됐어요",
            message = "업데이트를 마치려면 앱을 재시작해 주세요.",
            confirmText = "재시작",
            onConfirm = {
                showRestartDialog = false
                appUpdateManager.completeUpdate()
            },
            dismissText = "나중에",
            onDismiss = { showRestartDialog = false },
        )
    }
}
