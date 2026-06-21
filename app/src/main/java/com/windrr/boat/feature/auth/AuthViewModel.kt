package com.windrr.boat.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.model.LoginRequest
import com.windrr.boat.data.remote.model.RefreshRequest
import com.windrr.boat.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TERMS_VERSION   = "1.0"
private const val PRIVACY_VERSION = "1.0"

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        observeTokens()
    }

    private fun observeTokens() {
        viewModelScope.launch {
            authRepository.accessToken.collect { token ->
                // 첫 emit 시점에 토큰 확인 완료 → isInitializing 해제 (로그인 화면 깜빡임 방지)
                _state.update { it.copy(isLoggedIn = token != null, accessToken = token, isInitializing = false) }
            }
        }
    }

    fun handleIntent(intent: AuthIntent) {
        viewModelScope.launch {
            when (intent) {

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
                        val firebaseIdToken = getFirebaseIdToken(user)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                displayName = intent.displayName ?: user?.displayName,
                                email = intent.email ?: user?.email,
                                photoUrl = user?.photoUrl?.toString(),
                                requiresTerms = true,
                                pendingFirebaseToken = firebaseIdToken,
                            )
                        }
                    } catch (e: Exception) {
                        BoatLog.e("Google Firebase 인증 실패", e)
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
                        val firebaseIdToken = getFirebaseIdToken(user)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                displayName = intent.displayName ?: user?.displayName,
                                email = intent.email ?: user?.email,
                                photoUrl = user?.photoUrl?.toString(),
                                requiresTerms = true,
                                pendingFirebaseToken = firebaseIdToken,
                            )
                        }
                    } catch (e: Exception) {
                        BoatLog.e("Apple Firebase 인증 실패", e)
                        _state.update { it.copy(isLoading = false, error = e.message) }
                    }
                }

                is AuthIntent.CompleteTermsAndLogin -> {
                    val firebaseToken = _state.value.pendingFirebaseToken ?: run {
                        BoatLog.e("CompleteTermsAndLogin: pendingFirebaseToken 없음")
                        return@launch
                    }
                    _state.update { it.copy(isLoading = true, error = null) }
                    try {
                        val response = ApiClient.authApiService.login(
                            LoginRequest(
                                idToken          = firebaseToken,
                                termsVersion     = TERMS_VERSION,
                                privacyVersion   = PRIVACY_VERSION,
                                termsAccepted    = intent.termsAccepted,
                                privacyAccepted  = intent.privacyAccepted,
                                marketingConsent = intent.marketingConsent,
                            )
                        )
                        authRepository.saveTokens(response.data.accessToken, response.data.refreshToken)
                        BoatLog.i("로그인 완료 — termsAccepted=${intent.termsAccepted}, marketing=${intent.marketingConsent}")
                        _state.update { it.copy(isLoading = false, requiresTerms = false, pendingFirebaseToken = null) }
                    } catch (e: Exception) {
                        BoatLog.e("약관 동의 후 로그인 실패", e)
                        _state.update { it.copy(isLoading = false, error = e.message) }
                    }
                }

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

                is AuthIntent.SignOut -> {
                    // 서버 세션 revoke (best-effort) — refreshToken을 비우기 전에 호출.
                    // API 성공 여부와 무관하게 로컬 로그아웃은 항상 진행한다(네트워크 실패로 갇히지 않도록).
                    val refreshToken = authRepository.refreshToken.first()
                    if (!refreshToken.isNullOrBlank()) {
                        runCatching {
                            ApiClient.authApiService.logout(RefreshRequest(refreshToken))
                        }.onSuccess { res ->
                            if (res.isSuccessful) BoatLog.i("로그아웃 — 서버 세션 revoke 성공")
                            else BoatLog.e("로그아웃 API 응답 실패: code=${res.code()}")
                        }.onFailure { e ->
                            BoatLog.e("로그아웃 API 호출 실패 (로컬 로그아웃은 진행)", e)
                        }
                    }

                    FirebaseAuth.getInstance().signOut()
                    authRepository.clearTokens()
                    BoatLog.clearUser()
                    BoatLog.i("로그아웃")
                    _state.update { AuthState() }
                }

                is AuthIntent.DeleteAccount -> {
                    _state.update { it.copy(isLoading = true, error = null) }
                    val result = runCatching { ApiClient.authApiService.deleteAccount() }
                    val response = result.getOrNull()
                    if (response != null && response.isSuccessful) {
                        // 서버 계정 삭제 성공(204) → 로컬 세션/토큰 정리 → 로그인 화면 복귀
                        FirebaseAuth.getInstance().signOut()
                        authRepository.clearTokens()
                        BoatLog.clearUser()
                        BoatLog.i("회원 탈퇴 완료")
                        _state.update { AuthState() }
                    } else {
                        val code = response?.code()
                        BoatLog.e("회원 탈퇴 실패 (code=$code)", result.exceptionOrNull())
                        _state.update { it.copy(isLoading = false, error = "회원 탈퇴 실패") }
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

    private suspend fun getFirebaseIdToken(user: FirebaseUser?): String =
        suspendCancellableCoroutine<String?> { cont ->
            user?.getIdToken(false)
                ?.addOnSuccessListener { res -> cont.resume(res.token) }
                ?.addOnFailureListener { cont.resumeWithException(it) }
                ?: cont.resume(null)
        } ?: throw Exception("Firebase ID 토큰 획득 실패")

}
