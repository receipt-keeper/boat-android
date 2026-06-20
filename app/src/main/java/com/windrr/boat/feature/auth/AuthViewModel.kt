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
import com.windrr.boat.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.HttpException
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
                _state.update { it.copy(isLoggedIn = token != null, accessToken = token) }
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
                        val email       = intent.email       ?: user?.email
                        val displayName = intent.displayName ?: user?.displayName
                        _state.update { it.copy(displayName = displayName, email = email, photoUrl = user?.photoUrl?.toString()) }

                        callBackendLogin(user = user, email = email, displayName = displayName)
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
                        val email       = intent.email       ?: user?.email
                        val displayName = intent.displayName ?: user?.displayName
                        _state.update { it.copy(displayName = displayName, email = email, photoUrl = user?.photoUrl?.toString()) }

                        callBackendLogin(user = user, email = email, displayName = displayName)
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
                        BoatLog.i("신규 가입 완료 — termsAccepted=${intent.termsAccepted}, marketing=${intent.marketingConsent}")
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

    /** Firebase user로부터 Firebase ID 토큰을 얻어 백엔드 로그인 호출 */
    private suspend fun callBackendLogin(
        user: FirebaseUser?,
        email: String?,
        displayName: String?,
    ) {
        val firebaseIdToken = suspendCancellableCoroutine<String?> { cont ->
            user?.getIdToken(false)
                ?.addOnSuccessListener { res -> cont.resume(res.token) }
                ?.addOnFailureListener { cont.resumeWithException(it) }
                ?: cont.resume(null)
        } ?: throw Exception("Firebase ID 토큰 획득 실패")

        try {
            val response = ApiClient.authApiService.login(LoginRequest(idToken = firebaseIdToken))
            authRepository.saveTokens(response.data.accessToken, response.data.refreshToken)
            BoatLog.i("로그인 성공 (기존 유저) — email=$email, name=$displayName")
            _state.update { it.copy(isLoading = false) }
        } catch (e: HttpException) {
            when (e.code()) {
                422 -> {
                    BoatLog.i("신규 가입 — 약관 동의 필요, email=$email")
                    _state.update { it.copy(isLoading = false, requiresTerms = true, pendingFirebaseToken = firebaseIdToken) }
                }
                else -> {
                    BoatLog.e("백엔드 로그인 실패 (code=${e.code()})", e)
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }
    }
}
