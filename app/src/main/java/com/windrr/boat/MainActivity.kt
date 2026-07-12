package com.windrr.boat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.repository.AuthRepositoryImpl
import com.windrr.boat.feature.auth.AuthIntent
import com.windrr.boat.feature.auth.AuthViewModel
import com.windrr.boat.feature.auth.LoginScreen
import com.windrr.boat.feature.home.HomeActivity
import com.windrr.boat.feature.terms.TermsScreen
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.BoatTheme
import com.windrr.boat.ui.theme.ColorWhite

class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        trackAppInstallOnce()

        setContent {
            BoatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
                    val toastState = rememberBoatToastState()
                    val msgTermsCancelled = stringResource(R.string.login_error_cancelled)

                    BoatToastHost(state = toastState)

                    // API 호출 실패 시 에러 토스트 (서버 message 또는 네트워크 안내)
                    LaunchedEffect(state.error) {
                        state.error?.let {
                            toastState.showError(it)
                            authViewModel.clearError()
                        }
                    }

                    // 로그인 완료 시 HomeActivity로 전환
                    LaunchedEffect(state.isLoggedIn, state.requiresTerms) {
                        if (state.isLoggedIn && !state.requiresTerms) {
                            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                            finish()
                        }
                    }

                    when {
                        // 1) 토큰 확인 전, 2) 로그인 확인됐지만 HomeActivity 전환 직전 —
                        // 두 구간 모두 LoginScreen을 그리지 않고 빈 배경만 표시해 깜빡임 방지
                        state.isInitializing || (state.isLoggedIn && !state.requiresTerms) -> Box(
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(ColorWhite)
                        )
                        state.requiresTerms -> TermsScreen(
                            onBack = {
                                toastState.showError(msgTermsCancelled)
                                authViewModel.handleIntent(AuthIntent.SignOut)
                            },
                            onComplete = { termsAccepted, privacyAccepted, marketingConsent ->
                                authViewModel.handleIntent(
                                    AuthIntent.Signup(termsAccepted, privacyAccepted, marketingConsent)
                                )
                            },
                            modifier = Modifier.padding(innerPadding),
                        )
                        else -> LoginScreen(
                            viewModel = authViewModel,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }

    /** 앱 최초 실행 시 1회만 app_install 이벤트 전송 */
    private fun trackAppInstallOnce() {
        val prefs = getSharedPreferences("analytics_prefs", Context.MODE_PRIVATE)
        val isTracked = prefs.getBoolean("app_install_tracked", false)
        if (!isTracked) {
            firebaseAnalytics.logEvent("app_install", null)
            prefs.edit().putBoolean("app_install_tracked", true).apply()
        }
    }
}
