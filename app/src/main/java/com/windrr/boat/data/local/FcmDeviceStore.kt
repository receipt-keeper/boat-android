package com.windrr.boat.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.fcmDeviceDataStore: DataStore<Preferences> by preferencesDataStore(name = "fcm_device")

/**
 * 서버에 마지막으로 등록 성공한 FCM registration token을 캐시한다.
 *
 * 로그아웃 시 디바이스 해제(DELETE)에 이 값을 사용한다 — 로그아웃 시점에 토큰 조회가
 * 실패하거나 이미 회전된 경우에도, 서버에 등록해둔 바로 그 토큰으로 정확히 해제하기 위함.
 * 또한 앱 진입/토큰 갱신 시 "이미 서버에 올린 토큰과 같으면 재등록 생략"하는 데도 쓴다.
 */
class FcmDeviceStore(private val context: Context) {

    companion object {
        private val KEY_REGISTERED_TOKEN = stringPreferencesKey("registered_token")
    }

    suspend fun getRegisteredToken(): String? =
        context.fcmDeviceDataStore.data.first().let { it[KEY_REGISTERED_TOKEN] }

    suspend fun saveRegisteredToken(token: String) {
        context.fcmDeviceDataStore.edit { it[KEY_REGISTERED_TOKEN] = token }
    }

    suspend fun clear() {
        context.fcmDeviceDataStore.edit { it.clear() }
    }
}
