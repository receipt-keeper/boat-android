package com.windrr.boat.feature.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.windrr.boat.MainActivity
import com.windrr.boat.R
import com.windrr.boat.core.notification.FcmDeviceManager
import com.windrr.boat.feature.notification.NotificationPermissionGate
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.repository.AuthRepositoryImpl
import com.windrr.boat.feature.auth.AuthIntent
import com.windrr.boat.feature.auth.AuthViewModel
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.RefreshOnResume
import com.windrr.boat.ui.component.UserFeedbackViewModel
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.BoatTheme
import kotlinx.coroutines.flow.MutableStateFlow

class HomeActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_TARGET_TAB = "extra_target_tab"

        /** 홈 화면으로 이동하며 특정 탭을 선택하기 위한 Intent */
        fun intent(context: Context, targetTab: MainTab? = null): Intent =
            Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                targetTab?.let { putExtra(EXTRA_TARGET_TAB, it.route) }
            }
    }

    private val targetTabState = MutableStateFlow(MainTab.START)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 초기 인텐트 처리
        handleIntent(intent)

        setContent {
            BoatTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return AuthViewModel(
                                AuthRepositoryImpl(ApiClient.tokenDataStore),
                                com.windrr.boat.data.repository.UserRepositoryImpl(
                                    ApiClient.userDataStore,
                                    ApiClient.userApiService,
                                    ApiClient.notificationApiService,
                                    ApiClient.creditsApiService,
                                ),
                            ) as T
                        }
                    }
                )
                val state by authViewModel.state.collectAsState()

                val feedbackViewModel: UserFeedbackViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return UserFeedbackViewModel(
                                ApiClient.userDataStore,
                                FirebaseAnalytics.getInstance(this@HomeActivity)
                            ) as T
                        }
                    }
                )

                val targetTab by targetTabState.collectAsState()

                // 메인 진입 시(앱 시작/로그인 성공 모두 이 화면을 거침) 서버에서 내 정보 동기화
                LaunchedEffect(Unit) { authViewModel.syncUser() }
                // FCM 디바이스 등록 — 로그인 상태에서 멱등 upsert (PUT /notifications/devices)
                LaunchedEffect(Unit) { FcmDeviceManager.register() }

                // 알림 차단 상태면 앱 진입/복귀마다 권한 요청 또는 설정 유도
                NotificationPermissionGate()

                val toastState = rememberBoatToastState()
                val msgBackExit = stringResource(R.string.home_back_exit)

                BoatToastHost(state = toastState)

                // API 실패(탈퇴/동기화 등) 시 에러 토스트 (서버 message 또는 네트워크 안내)
                LaunchedEffect(state.error) {
                    state.error?.let {
                        toastState.showError(it)
                        authViewModel.clearError()
                    }
                }

                // 토큰이 사라지면(직접 로그아웃 또는 refresh 실패에 의한 강제 로그아웃) 로그인 화면으로 복귀.
                // 진입 직후 DataStore가 아직 토큰을 emit하기 전의 초기 false는 무시하기 위해
                // "한 번이라도 로그인 상태였는지"를 기준으로 판단한다.
                var hasBeenLoggedIn by remember { mutableStateOf(false) }
                LaunchedEffect(state.isLoggedIn) {
                    if (state.isLoggedIn) {
                        hasBeenLoggedIn = true
                    } else if (hasBeenLoggedIn) {
                        startActivity(
                            Intent(this@HomeActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                        finish()
                    }
                }

                Box(Modifier.fillMaxSize()) {
                    MainScreen(
                        user = state.user,
                        initialTab = targetTab,
                        onTabSelected = { targetTabState.value = it },
                        onSignOut = { authViewModel.handleIntent(AuthIntent.SignOut) },
                        onDeleteAccount = { authViewModel.handleIntent(AuthIntent.DeleteAccount) },
                        onShowExitToast = { toastState.show(msgBackExit) },
                        toastState = toastState,
                        feedbackViewModel = feedbackViewModel,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val route = intent.getStringExtra(EXTRA_TARGET_TAB)
        MainTab.entries.find { it.route == route }?.let {
            targetTabState.value = it
        }
    }
}
