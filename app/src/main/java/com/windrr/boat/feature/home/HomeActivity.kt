package com.windrr.boat.feature.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.MainActivity
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.repository.AuthRepositoryImpl
import com.windrr.boat.feature.auth.AuthIntent
import com.windrr.boat.feature.auth.AuthViewModel
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.BoatTheme

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                                ),
                            ) as T
                        }
                    }
                )
                val state by authViewModel.state.collectAsState()

                // 메인 진입 시(앱 시작/로그인 성공 모두 이 화면을 거침) 서버에서 내 정보 동기화
                LaunchedEffect(Unit) { authViewModel.syncUser() }

                val toastState = rememberBoatToastState()

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

                MainScreen(
                    user = state.user,
                    onSignOut = { authViewModel.handleIntent(AuthIntent.SignOut) },
                    onDeleteAccount = { authViewModel.handleIntent(AuthIntent.DeleteAccount) },
                )
            }
        }
    }
}
