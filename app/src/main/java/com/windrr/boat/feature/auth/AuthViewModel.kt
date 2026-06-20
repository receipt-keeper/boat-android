package com.windrr.boat.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
                        BoatLog.e("토큰 저장 실패", e)
                        _state.update { it.copy(isLoading = false, error = e.message) }
                        return@launch
                    }
                    _state.update { it.copy(isLoading = false) }
                }

                is AuthIntent.RefreshAccessToken -> {
                    runCatching {
                        authRepository.updateAccessToken(intent.newAccessToken)
                    }.onFailure { e ->
                        BoatLog.e("토큰 갱신 실패", e)
                        _state.update { it.copy(error = e.message) }
                    }
                }

                is AuthIntent.SignInWithGoogle -> {
                    _state.update { it.copy(isLoading = true, error = null) }
                    try {
                        val credential = GoogleAuthProvider.getCredential(intent.idToken, null)
                        val result = suspendCancellableCoroutine { cont ->
                            FirebaseAuth.getInstance()
                                .signInWithCredential(credential)
                                .addOnSuccessListener { cont.resume(it) }
                                .addOnFailureListener { cont.resumeWithException(it) }
                        }
                        val user = result.user
                        user?.uid?.let { BoatLog.setUser(it) }
                        // Google Account에서 직접 가져온 값 우선, 없으면 Firebase user fallback
                        val email = intent.email ?: user?.email
                        val displayName = intent.displayName ?: user?.displayName
                        BoatLog.i("Google 로그인 성공 — email=$email, name=$displayName")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                displayName = displayName,
                                email = email,
                                photoUrl = user?.photoUrl?.toString()
                            )
                        }
                        // TODO: 서버에 Firebase idToken 전달 → JWT 발급
                    } catch (e: Exception) {
                        BoatLog.e("Google 로그인 실패", e)
                        _state.update { it.copy(isLoading = false, error = e.message) }
                    }
                }

                is AuthIntent.SignInWithApple -> {
                    _state.update { it.copy(isLoading = true, error = null) }
                    try {
                        val credential = OAuthProvider.newCredentialBuilder("apple.com")
                            .setIdToken(intent.idToken)
                            .build()
                        val result = suspendCancellableCoroutine { cont ->
                            FirebaseAuth.getInstance()
                                .signInWithCredential(credential)
                                .addOnSuccessListener { cont.resume(it) }
                                .addOnFailureListener { cont.resumeWithException(it) }
                        }
                        val user = result.user
                        user?.uid?.let { BoatLog.setUser(it) }
                        // 최초 로그인 시 Apple이 제공한 값 우선, 이후엔 Firebase user fallback
                        val email = intent.email ?: user?.email
                        val displayName = intent.displayName ?: user?.displayName
                        BoatLog.i("Apple 로그인 성공 — email=$email, name=$displayName")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                displayName = displayName,
                                email = email,
                                photoUrl = user?.photoUrl?.toString()
                            )
                        }
                        // TODO: 서버에 Firebase idToken 전달 → JWT 발급
                    } catch (e: Exception) {
                        BoatLog.e("Apple 로그인 실패", e)
                        _state.update { it.copy(isLoading = false, error = e.message) }
                    }
                }

                is AuthIntent.SignOut -> {
                    FirebaseAuth.getInstance().signOut()
                    authRepository.clearTokens()
                    BoatLog.clearUser()
                    BoatLog.i("로그아웃")
                    _state.update { AuthState() }
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
