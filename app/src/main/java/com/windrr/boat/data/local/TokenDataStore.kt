package com.windrr.boat.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

class TokenDataStore(private val context: Context) {

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    /** 저장된 AccessToken을 Flow로 관찰 */
    val accessToken: Flow<String?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_ACCESS_TOKEN] }

    /** 저장된 RefreshToken을 Flow로 관찰 */
    val refreshToken: Flow<String?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_REFRESH_TOKEN] }

    /** 로그인 성공 시 두 토큰 모두 저장 */
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    /** AccessToken만 갱신 (토큰 재발급 시) */
    suspend fun updateAccessToken(newAccessToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = newAccessToken
        }
    }

    /** 로그아웃 시 토큰 전체 삭제 */
    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
        }
    }
}
