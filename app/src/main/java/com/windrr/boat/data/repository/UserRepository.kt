package com.windrr.boat.data.repository

import com.windrr.boat.data.local.UserDataStore
import com.windrr.boat.data.model.User
import com.windrr.boat.data.remote.CreditsApiService
import com.windrr.boat.data.remote.NotificationApiService
import com.windrr.boat.data.remote.UserApiService
import com.windrr.boat.data.remote.model.UpdateNotificationSettingsRequest
import com.windrr.boat.data.remote.model.toUser
import kotlinx.coroutines.flow.Flow

/**
 * 사용자 정보 데이터 접근을 추상화하는 Repository 인터페이스.
 * ViewModel은 구현체가 아닌 이 인터페이스에만 의존한다.
 */
interface UserRepository {
    /** 저장된 사용자 정보를 관찰하는 Flow */
    val user: Flow<User>

    /** 서버에서 내 정보 + 알림 설정을 조회 후 로컬에 캐시. 성공 시 최신 User 반환 */
    suspend fun refreshUser(): Result<User>

    /** 알림 설정만 서버에서 조회해 로컬 캐시 갱신 — GET /api/v1/notifications/settings */
    suspend fun refreshNotificationSettings(): Result<Unit>

    /** 알림 설정 수정 — PATCH /api/v1/notifications/settings */
    suspend fun updateMe(notificationEnabled: Boolean? = null, marketingConsent: Boolean? = null): Result<Unit>

    /** 사용자 정보 전체 저장 */
    suspend fun saveUser(user: User)

    /** 알림 수신 설정만 갱신 */
    suspend fun updateNotificationEnabled(enabled: Boolean)

    /** 마케팅 수신 동의만 갱신 */
    suspend fun updateMarketingConsent(consent: Boolean)

    /** 남은 무료 분석 토큰 수만 갱신 */
    suspend fun updateFreeAnalysisTokens(remaining: Int)

    /** 로그아웃/탈퇴 시 사용자 정보 전체 삭제 */
    suspend fun clear()
}

class UserRepositoryImpl(
    private val userDataStore: UserDataStore,
    private val userApiService: UserApiService,
    private val notificationApiService: NotificationApiService,
    private val creditsApiService: CreditsApiService,
) : UserRepository {

    override val user: Flow<User> = userDataStore.user

    /**
     * 프로필(GET /users/me) · 알림 설정(GET /notifications/settings) · 크레딧(GET /credits)
     * 세 API를 순차 조회 후 병합하여 로컬에 캐시.
     */
    override suspend fun refreshUser(): Result<User> = runCatching {
        val profileData = userApiService.getMe().data.toUser()
        val notifData   = notificationApiService.getNotificationSettings().data
        val creditsData = creditsApiService.getCredits().data
        val user = profileData.copy(
            notificationEnabled          = notifData.pushEnabled,
            marketingConsent             = notifData.marketingConsent,
            freeAnalysisTokensRemaining  = creditsData.remainingCount,
        )
        userDataStore.saveUser(user)
        user
    }

    /** 알림 설정만 조회 — GET /api/v1/notifications/settings. 두 토글(알림/마케팅)을 로컬에 반영. */
    override suspend fun refreshNotificationSettings(): Result<Unit> = runCatching {
        val data = notificationApiService.getNotificationSettings().data
        userDataStore.updateNotificationEnabled(data.pushEnabled)
        userDataStore.updateMarketingConsent(data.marketingConsent)
        Unit
    }

    /**
     * 알림 설정 부분 수정 — PATCH /api/v1/notifications/settings.
     * null 필드는 요청 바디에서 제외되어 기존 값이 유지된다.
     */
    override suspend fun updateMe(notificationEnabled: Boolean?, marketingConsent: Boolean?): Result<Unit> =
        runCatching {
            // 1) 낙관적 로컬 반영
            notificationEnabled?.let { userDataStore.updateNotificationEnabled(it) }
            marketingConsent?.let { userDataStore.updateMarketingConsent(it) }
            // 2) 서버 부분 수정 (pushEnabled = notificationEnabled)
            val res = notificationApiService.updateNotificationSettings(
                UpdateNotificationSettingsRequest(
                    pushEnabled = notificationEnabled,
                    marketingConsent = marketingConsent,
                )
            )
            // 3) 서버 확정 값으로 재동기화 — 요청에 포함한 필드만 반영해
            //    변경하지 않은 다른 토글 값을 응답으로 덮어쓰지 않는다.
            if (notificationEnabled != null) userDataStore.updateNotificationEnabled(res.data.pushEnabled)
            if (marketingConsent != null) userDataStore.updateMarketingConsent(res.data.marketingConsent)
            Unit
        }

    override suspend fun saveUser(user: User) = userDataStore.saveUser(user)

    override suspend fun updateNotificationEnabled(enabled: Boolean) =
        userDataStore.updateNotificationEnabled(enabled)

    override suspend fun updateMarketingConsent(consent: Boolean) =
        userDataStore.updateMarketingConsent(consent)

    override suspend fun updateFreeAnalysisTokens(remaining: Int) =
        userDataStore.updateFreeAnalysisTokens(remaining)

    override suspend fun clear() = userDataStore.clear()
}
