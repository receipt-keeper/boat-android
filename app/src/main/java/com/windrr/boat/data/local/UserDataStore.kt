package com.windrr.boat.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.windrr.boat.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// 토큰(auth_tokens)과 분리된 사용자 프로필 전용 DataStore
private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

/**
 * 사용자 정보를 Preferences DataStore에 저장/관찰하는 로컬 데이터 소스.
 */
class UserDataStore(private val context: Context) {

    companion object {
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_NAME = stringPreferencesKey("name")
        private val KEY_NICKNAME = stringPreferencesKey("nickname")
        private val KEY_PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
        private val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val KEY_MARKETING_CONSENT = booleanPreferencesKey("marketing_consent")
        private val KEY_FREE_ANALYSIS_TOKENS = intPreferencesKey("free_analysis_tokens_remaining")
        private val KEY_LAST_NOTIF_VIEWED_AT = stringPreferencesKey("last_notif_viewed_at")
    }

    /** 저장된 사용자 정보를 Flow로 관찰 (값이 없으면 기본값) */
    val user: Flow<User> = context.userDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            User(
                email = prefs[KEY_EMAIL] ?: "",
                name = prefs[KEY_NAME] ?: "",
                nickname = prefs[KEY_NICKNAME] ?: "",
                profileImageUrl = prefs[KEY_PROFILE_IMAGE_URL] ?: "",
                notificationEnabled = prefs[KEY_NOTIFICATION_ENABLED] ?: false,
                marketingConsent = prefs[KEY_MARKETING_CONSENT] ?: false,
                freeAnalysisTokensRemaining = prefs[KEY_FREE_ANALYSIS_TOKENS] ?: 0,
            )
        }

    /** 사용자 정보 전체 저장 (로그인 성공 / 프로필 조회 시) */
    suspend fun saveUser(user: User) {
        context.userDataStore.edit { prefs ->
            prefs[KEY_EMAIL] = user.email
            prefs[KEY_NAME] = user.name
            prefs[KEY_NICKNAME] = user.nickname
            prefs[KEY_PROFILE_IMAGE_URL] = user.profileImageUrl
            prefs[KEY_NOTIFICATION_ENABLED] = user.notificationEnabled
            prefs[KEY_MARKETING_CONSENT] = user.marketingConsent
            prefs[KEY_FREE_ANALYSIS_TOKENS] = user.freeAnalysisTokensRemaining
        }
    }

    /** 알림 수신 설정만 갱신 */
    suspend fun updateNotificationEnabled(enabled: Boolean) {
        context.userDataStore.edit { it[KEY_NOTIFICATION_ENABLED] = enabled }
    }

    /** 마케팅 수신 동의만 갱신 */
    suspend fun updateMarketingConsent(consent: Boolean) {
        context.userDataStore.edit { it[KEY_MARKETING_CONSENT] = consent }
    }

    /** 남은 무료 분석 토큰 수만 갱신 */
    suspend fun updateFreeAnalysisTokens(remaining: Int) {
        context.userDataStore.edit { it[KEY_FREE_ANALYSIS_TOKENS] = remaining }
    }

    /** 마지막으로 알림 목록을 확인한 시각 (ISO 8601) */
    val lastNotifViewedAt: Flow<String?> = context.userDataStore.data
        .map { it[KEY_LAST_NOTIF_VIEWED_AT] }

    /** 마지막으로 알림 목록을 확인한 시각 갱신 */
    suspend fun updateLastNotifViewedAt(timestamp: String) {
        context.userDataStore.edit { it[KEY_LAST_NOTIF_VIEWED_AT] = timestamp }
    }

    /** 로그아웃/탈퇴 시 사용자 정보 전체 삭제 */
    suspend fun clear() {
        context.userDataStore.edit { it.clear() }
    }
}
