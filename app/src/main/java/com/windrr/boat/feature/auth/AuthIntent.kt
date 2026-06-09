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

    /** 로그아웃 — 저장된 토큰 전체 삭제 */
    data object Logout : AuthIntent()
}
