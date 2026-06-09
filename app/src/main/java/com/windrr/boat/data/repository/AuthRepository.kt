package com.windrr.boat.data.repository

import com.windrr.boat.data.local.TokenDataStore
import kotlinx.coroutines.flow.Flow

/**
 * 인증 관련 데이터 접근을 추상화하는 Repository 인터페이스
 * ViewModel은 구현체가 아닌 이 인터페이스에만 의존
 *
 * @property accessToken 저장된 AccessToken을 관찰하는 Flow
 * @property refreshToken 저장된 RefreshToken을 관찰하는 Flow
 */
interface AuthRepository {
    val accessToken: Flow<String?>
    val refreshToken: Flow<String?>

    /** 로그인 성공 시 두 토큰 모두 저장 */
    suspend fun saveTokens(accessToken: String, refreshToken: String)

    /** AccessToken만 갱신 (토큰 재발급 시) */
    suspend fun updateAccessToken(newAccessToken: String)

    /** 로그아웃 시 토큰 전체 삭제 */
    suspend fun clearTokens()
}

class AuthRepositoryImpl(
    private val tokenDataStore: TokenDataStore
) : AuthRepository {

    override val accessToken: Flow<String?> = tokenDataStore.accessToken

    override val refreshToken: Flow<String?> = tokenDataStore.refreshToken

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        tokenDataStore.saveTokens(accessToken, refreshToken)
    }

    override suspend fun updateAccessToken(newAccessToken: String) {
        tokenDataStore.updateAccessToken(newAccessToken)
    }

    override suspend fun clearTokens() {
        tokenDataStore.clearTokens()
    }
}
