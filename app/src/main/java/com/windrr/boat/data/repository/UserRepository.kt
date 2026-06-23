package com.windrr.boat.data.repository

import com.windrr.boat.data.local.UserDataStore
import com.windrr.boat.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 사용자 정보 데이터 접근을 추상화하는 Repository 인터페이스.
 * ViewModel은 구현체가 아닌 이 인터페이스에만 의존한다.
 */
interface UserRepository {
    /** 저장된 사용자 정보를 관찰하는 Flow */
    val user: Flow<User>

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
    private val userDataStore: UserDataStore
) : UserRepository {

    override val user: Flow<User> = userDataStore.user

    override suspend fun saveUser(user: User) = userDataStore.saveUser(user)

    override suspend fun updateNotificationEnabled(enabled: Boolean) =
        userDataStore.updateNotificationEnabled(enabled)

    override suspend fun updateMarketingConsent(consent: Boolean) =
        userDataStore.updateMarketingConsent(consent)

    override suspend fun updateFreeAnalysisTokens(remaining: Int) =
        userDataStore.updateFreeAnalysisTokens(remaining)

    override suspend fun clear() = userDataStore.clear()
}
