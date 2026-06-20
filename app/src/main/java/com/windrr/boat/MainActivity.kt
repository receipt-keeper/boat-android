package com.windrr.boat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val authViewModel: AuthViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return AuthViewModel(
                                    AuthRepositoryImpl(ApiClient.tokenDataStore)
                                ) as T
                            }
                        }
                    )
                    val state by authViewModel.state.collectAsState()
                    val toastState = rememberBoatToastState()
                    val msgTermsCancelled = stringResource(R.string.login_error_cancelled)
                    val msgServerError    = stringResource(R.string.error_server_retry)

                    BoatToastHost(state = toastState)

                    // API 호출 실패 시 에러 토스트
                    LaunchedEffect(state.error) {
                        if (state.error != null) toastState.showError(msgServerError)
                    }

                    // 로그인 완료 시 HomeActivity로 전환
                    LaunchedEffect(state.isLoggedIn, state.requiresTerms) {
                        if (state.isLoggedIn && !state.requiresTerms) {
                            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                            finish()
                        }
                    }

                    when {
                        state.requiresTerms -> TermsScreen(
                            onBack = {
                                toastState.showError(msgTermsCancelled)
                                authViewModel.handleIntent(AuthIntent.SignOut)
                            },
                            onComplete = { termsAccepted, privacyAccepted, marketingConsent ->
                                authViewModel.handleIntent(
                                    AuthIntent.CompleteTermsAndLogin(termsAccepted, privacyAccepted, marketingConsent)
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
}
