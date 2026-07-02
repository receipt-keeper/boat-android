package com.windrr.boat.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.ApiErrorParser
import com.windrr.boat.data.model.User
import com.windrr.boat.data.remote.model.LoginRequest
import com.windrr.boat.data.remote.model.RefreshRequest
import com.windrr.boat.data.remote.model.SignupRequest
import com.windrr.boat.data.repository.AuthRepository
import com.windrr.boat.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.HttpException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        observeTokens()
        observeUser()
    }

    /** 저장된 사용자 정보를 관찰해 state에 반영 (앱 재시작 후에도 프로필 유지) */
    private fun observeUser() {
        viewModelScope.launch {
            userRepository.user.collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
    }

    /**
     * 서버에서 내 정보(GET /users/me)를 조회해 로컬에 동기화.
     * 앱 시작(로그인 상태) 또는 로그인/가입 성공 후 메인 진입 시 호출.
     * 성공 시 결과는 observeUser를 통해 state.user에 자동 반영된다.
     */
    fun syncUser() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            userRepository.refreshUser()
                .onFailure {
                    BoatLog.e("내 정보 동기화 실패", it)
                    _state.update { s -> s.copy(error = ApiErrorParser.message(it)) }
                }
            _state.update { it.copy(isSyncing = false) }
        }
    }

    /** 에러 토스트 표시 후 호출 — 동일 에러 재발생 시에도 다시 트리거되도록 비운다 */
    fun clearError() {
        _state.update { it.copy(error = null) }
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
                        loginOrRedirectToSignup(
                            firebaseIdToken = firebaseIdToken,
                            displayName = intent.displayName ?: user?.displayName,
                            email = intent.email ?: user?.email,
                            photoUrl = user?.photoUrl?.toString(),
                        )
                    } catch (e: Exception) {
                        BoatLog.e("Google Firebase 인증 실패", e)
                        _state.update { it.copy(isLoading = false, error = ApiErrorParser.message(e)) }
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
                        loginOrRedirectToSignup(
                            firebaseIdToken = firebaseIdToken,
                            displayName = intent.displayName ?: user?.displayName,
                            email = intent.email ?: user?.email,
                            photoUrl = user?.photoUrl?.toString(),
                        )
                    } catch (e: Exception) {
                        BoatLog.e("Apple Firebase 인증 실패", e)
                        _state.update { it.copy(isLoading = false, error = ApiErrorParser.message(e)) }
                    }
                }

                is AuthIntent.Signup -> {
                    val firebaseToken = _state.value.pendingFirebaseToken ?: run {
                        BoatLog.e("Signup: pendingFirebaseToken 없음")
                        return@launch
                    }
                    _state.update { it.copy(isLoading = true, error = null) }
                    try {
                        val response = ApiClient.authApiService.signup(
                            SignupRequest(
                                idToken          = firebaseToken,
                                termsAccepted    = intent.termsAccepted,
                                privacyAccepted  = intent.privacyAccepted,
                                marketingConsent = intent.marketingConsent,
                            )
                        )
                        authRepository.saveTokens(response.data.accessToken, response.data.refreshToken)

                        // HomeActivity의 syncUser()가 GET /users/me로 실제 정보를 채우기 전
                        // 소셜 로그인 기본 정보를 임시 저장.
                        val current = _state.value
                        userRepository.saveUser(
                            User(
                                email = current.email ?: "",
                                name = current.displayName ?: "",
                                profileImageUrl = current.photoUrl ?: "",
                                notificationEnabled = true,
                                marketingConsent = intent.marketingConsent,
                            )
                        )

                        BoatLog.i("회원가입 완료 — marketing=${intent.marketingConsent}")
                        _state.update { it.copy(isLoading = false, requiresTerms = false, pendingFirebaseToken = null) }
                    } catch (e: Exception) {
                        BoatLog.e("회원가입 실패", e)
                        _state.update { it.copy(isLoading = false, error = ApiErrorParser.message(e)) }
                    }
                }

                is AuthIntent.SaveTokens -> {
                    _state.update { it.copy(isLoading = true, error = null) }
                    runCatching {
                        authRepository.saveTokens(intent.accessToken, intent.refreshToken)
                    }.onFailure { e ->
                        BoatLog.e("토큰 저장 실패", e)
                        _state.update { it.copy(isLoading = false, error = ApiErrorParser.message(e)) }
                        return@launch
                    }
                    _state.update { it.copy(isLoading = false) }
                }

                is AuthIntent.RefreshAccessToken -> {
                    runCatching {
                        authRepository.updateAccessToken(intent.newAccessToken)
                    }.onFailure { e ->
                        BoatLog.e("토큰 갱신 실패", e)
                        _state.update { it.copy(error = ApiErrorParser.message(e)) }
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
                    userRepository.clear()
                    BoatLog.clearUser()
                    BoatLog.i("로그아웃")
                    _state.update { AuthState() }
                }

                is AuthIntent.DeleteAccount -> {
                    _state.update { it.copy(isLoading = true, error = null) }
                    val result = runCatching { ApiClient.userApiService.deleteAccount() }
                    val response = result.getOrNull()
                    if (response != null && response.isSuccessful) {
                        // 서버 계정 삭제 성공(204) → 로컬 세션/토큰/사용자정보 정리 → 로그인 화면 복귀
                        FirebaseAuth.getInstance().signOut()
                        authRepository.clearTokens()
                        userRepository.clear()
                        BoatLog.clearUser()
                        BoatLog.i("회원 탈퇴 완료")
                        _state.update { AuthState() }
                    } else {
                        val message = response?.let { ApiErrorParser.message(it) }
                            ?: result.exceptionOrNull()?.let { ApiErrorParser.message(it) }
                            ?: ApiErrorParser.NETWORK_MESSAGE
                        BoatLog.e("회원 탈퇴 실패 (code=${response?.code()})", result.exceptionOrNull())
                        _state.update { it.copy(isLoading = false, error = message) }
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

    /**
     * POST /api/v1/auth/login 시도.
     * - 200 성공 → 토큰 저장 (isLoggedIn 트리거 → HomeActivity 전환)
     * - 404 미가입 → requiresTerms = true (TermsScreen 진입)
     * - 그 외 오류 → 에러 토스트
     *
     * 호출 전 isLoading = true 가 이미 세팅되어 있어야 한다.
     */
    private suspend fun loginOrRedirectToSignup(
        firebaseIdToken: String,
        displayName: String?,
        email: String?,
        photoUrl: String?,
    ) {
        try {
            val response = ApiClient.authApiService.login(LoginRequest(idToken = firebaseIdToken))
            authRepository.saveTokens(response.data.accessToken, response.data.refreshToken)
            BoatLog.i("로그인 성공")
            _state.update { it.copy(isLoading = false) }
        } catch (e: HttpException) {
            if (e.code() == 404) {
                BoatLog.i("미가입 사용자 → 약관 동의 화면으로 이동")
                _state.update {
                    it.copy(
                        isLoading = false,
                        displayName = displayName,
                        email = email,
                        photoUrl = photoUrl,
                        requiresTerms = true,
                        pendingFirebaseToken = firebaseIdToken,
                    )
                }
            } else {
                BoatLog.e("로그인 API 오류 (code=${e.code()})", e)
                _state.update { it.copy(isLoading = false, error = ApiErrorParser.message(e)) }
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
