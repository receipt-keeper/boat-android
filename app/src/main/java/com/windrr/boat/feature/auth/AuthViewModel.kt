package com.windrr.boat.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * [MVI - ViewModel]
 * Intent를 받아서 처리하고, State를 갱신하는 유일한 주체
 * View는 state를 구독만 하고, handleIntent()로 이벤트만 전달
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    /** 외부에는 읽기 전용 StateFlow만 노출 */
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        observeTokens()
    }

    /** 앱 시작 시 DataStore의 토큰 상태를 즉시 반영 */
    private fun observeTokens() {
        viewModelScope.launch {
            authRepository.accessToken.collect { token ->
                _state.update { currentState ->
                    currentState.copy(
                        isLoggedIn = token != null,
                        accessToken = token
                    )
                }
            }
        }
    }

    /**
     * View에서 발생한 Intent를 처리하는 단일 진입점
     * when 분기로 각 Intent에 맞는 동작 수행
     *
     * @param intent 처리할 [AuthIntent]
     */
    fun handleIntent(intent: AuthIntent) {
        viewModelScope.launch {
            when (intent) {
                is AuthIntent.SaveTokens -> {
                    _state.update { it.copy(isLoading = true, error = null) }
                    runCatching {
                        authRepository.saveTokens(intent.accessToken, intent.refreshToken)
                    }.onFailure { e ->
                        _state.update { it.copy(isLoading = false, error = e.message) }
                        return@launch
                    }
                    _state.update { it.copy(isLoading = false) }
                }

                is AuthIntent.RefreshAccessToken -> {
                    runCatching {
                        authRepository.updateAccessToken(intent.newAccessToken)
                    }.onFailure { e ->
                        _state.update { it.copy(error = e.message) }
                    }
                }

                is AuthIntent.Logout -> {
                    _state.update { it.copy(isLoading = true) }
                    authRepository.clearTokens()
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}
