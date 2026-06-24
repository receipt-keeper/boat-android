package com.windrr.boat.data.model

/**
 * 앱에서 사용하는 사용자 정보 모델.
 *
 * @property email                       사용자 이메일
 * @property name                        사용자 이름
 * @property nickname                    닉네임
 * @property profileImageUrl             프로필 이미지 URL
 * @property notificationEnabled         알림 수신 설정. true면 푸시 알림을 받는다.
 * @property marketingConsent            마케팅 수신 동의 여부
 * @property freeAnalysisTokensRemaining 남은 무료 분석 토큰 수
 */
data class User(
    val email: String = "",
    val name: String = "",
    val nickname: String = "",
    val profileImageUrl: String = "",
    val notificationEnabled: Boolean = false,
    val marketingConsent: Boolean = false,
    val freeAnalysisTokensRemaining: Int = 0,
) {
    /** 표시용 이름 — 닉네임 우선, 없으면 이름 */
    val displayName: String get() = nickname.ifBlank { name }
}
