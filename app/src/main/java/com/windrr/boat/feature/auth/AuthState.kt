package com.windrr.boat.feature.auth

/**
 * [MVI - Model]
 * 인증 화면의 UI 상태를 나타내는 불변 데이터 클래스
 *
 * @property isInitializing   앱 시작 시 DataStore 토큰 확인 중 여부(첫 emit 전 true) → 로그인 화면 깜빡임 방지
 * @property isLoggedIn       백엔드 토큰 저장 완료 → DataStore 구독에 의해 자동 갱신
 * @property requiresTerms    신규 가입 422 응답 → TermsScreen 진입 트리거
 * @property pendingFirebaseToken 약관 동의 후 백엔드 재호출에 사용할 Firebase ID 토큰
 */
data class AuthState(
    val isInitializing: Boolean = true,
    val isLoggedIn: Boolean = false,
    val accessToken: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val requiresTerms: Boolean = false,
    val pendingFirebaseToken: String? = null,
)
