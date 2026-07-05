package com.windrr.boat.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.fcmDeviceDataStore: DataStore<Preferences> by preferencesDataStore(name = "fcm_device")

/**
 * 서버에 마지막으로 등록 성공한 FID(Firebase Installation ID)를 캐시한다.
 *
 * 로그아웃 시 디바이스 해제(DELETE) 호출에 이 값을 사용한다 — 로그아웃 시점에
 * Installations 조회가 실패하거나 FID가 이미 삭제/회전된 경우에도, 서버에 등록해둔
 * 바로 그 FID로 정확히 해제할 수 있도록 보관해둔다.
 */
class FcmDeviceStore(private val context: Context) {

    companion object {
        private val KEY_REGISTERED_FID = stringPreferencesKey("registered_fid")
    }

    suspend fun getRegisteredFid(): String? =
        context.fcmDeviceDataStore.data.first().let { it[KEY_REGISTERED_FID] }

    suspend fun saveRegisteredFid(fid: String) {
        context.fcmDeviceDataStore.edit { it[KEY_REGISTERED_FID] = fid }
    }

    suspend fun clear() {
        context.fcmDeviceDataStore.edit { it.clear() }
    }
}
