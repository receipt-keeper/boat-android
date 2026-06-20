package com.windrr.boat.feature.auth

/**
 * [MVI - Intent]
 * 사용자가 인증 화면에서 발생시킬 수 있는 이벤트 목록
 */
sealed class AuthIntent {

    /**
     * 로그인 성공 후 서버로부터 두 토큰을 받아 저장
     *
     * @property accessToken 서버로부터 발급받은 AccessToken
     * @property refreshToken 서버로부터 발급받은 RefreshToken
     */
    data class SaveTokens(
        val accessToken: String,
        val refreshToken: String
    ) : AuthIntent()

    /**
     * AccessToken 만료 시 재발급된 토큰으로 갱신
     *
     * @property newAccessToken 재발급된 AccessToken
     */
    data class RefreshAccessToken(
        val newAccessToken: String
    ) : AuthIntent()

    /**
     * Google 로그인 성공 후 Firebase 인증 처리
     *
     * @property idToken Google Sign-In으로 발급받은 idToken
     * @property email Google 계정 이메일
     * @property displayName Google 계정 이름
     */
    data class SignInWithGoogle(
        val idToken: String,
        val email: String? = null,
        val displayName: String? = null,
    ) : AuthIntent()

    /**
     * Apple 로그인 성공 후 Firebase 인증 처리
     *
     * @property idToken Apple Sign-In으로 발급받은 idToken
     * @property displayName Apple에서 제공하는 사용자 이름 (최초 로그인 시에만 제공)
     * @property email Apple에서 제공하는 이메일
     */
    data class SignInWithApple(
        val idToken: String,
        val displayName: String? = null,
        val email: String? = null,
    ) : AuthIntent()

    /**
     * 약관 동의 완료 후 백엔드 로그인 재시도 (신규 가입)
     *
     * @property termsAccepted    서비스 이용약관 동의 여부
     * @property privacyAccepted  개인정보 처리방침 동의 여부
     * @property marketingConsent 마케팅 수신 동의 여부
     */
    data class CompleteTermsAndLogin(
        val termsAccepted: Boolean,
        val privacyAccepted: Boolean,
        val marketingConsent: Boolean,
    ) : AuthIntent()

    /** 로그아웃 — Firebase 세션 + DataStore 토큰 전체 삭제 */
    data object SignOut : AuthIntent()

    /** 로그아웃 — 저장된 토큰 전체 삭제 (서버 JWT 연동 후 사용) */
    data object Logout : AuthIntent()
}
