package com.windrr.boat.feature.auth

/**
 * [MVI - Model]
 * 인증 화면의 UI 상태를 나타내는 불변 데이터 클래스
 * View는 이 State만 보고 화면을 그림 — 로직 판단 없음
 *
 * @property isLoggedIn 로그인 여부
 * @property accessToken 현재 유효한 AccessToken
 * @property displayName Google 계정 표시 이름
 * @property email Google 계정 이메일
 * @property photoUrl Google 프로필 이미지 URL
 * @property isLoading 로그인/로그아웃 처리 중 여부
 * @property error 에러 메시지 (null이면 에러 없음)
 */
data class AuthState(
    val isLoggedIn: Boolean = false,
    val accessToken: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
