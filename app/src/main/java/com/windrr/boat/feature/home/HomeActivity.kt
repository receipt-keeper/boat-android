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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.MainActivity
import com.windrr.boat.R
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
                                AuthRepositoryImpl(ApiClient.tokenDataStore)
                            ) as T
                        }
                    }
                )
                val state by authViewModel.state.collectAsState()
                val toastState = rememberBoatToastState()
                val msgDeleteError = stringResource(R.string.account_delete_error)

                BoatToastHost(state = toastState)

                // 탈퇴 등 API 실패 시 에러 토스트
                LaunchedEffect(state.error) {
                    if (state.error != null) toastState.showError(msgDeleteError)
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

                HomeScreen(
                    displayName = state.displayName,
                    onSignOut = { authViewModel.handleIntent(AuthIntent.SignOut) },
                    onDeleteAccount = { authViewModel.handleIntent(AuthIntent.DeleteAccount) },
                )
            }
        }
    }
}
