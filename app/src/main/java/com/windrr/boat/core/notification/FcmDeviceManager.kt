package com.windrr.boat.core.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.model.RegisterDeviceRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * FCM 디바이스(등록 토큰) 등록/해제 관리.
 *
 * 서버는 디바이스를 FCM registration token으로 식별한다.
 * 토큰은 발급/갱신 시 [BoatFirebaseMessagingService.onNewToken] 콜백으로 통지되므로,
 * 앱 진입 시 1회 등록 + 갱신 시 재등록 조합으로 최신 상태를 유지한다.
 *
 * - [register]:   로그인 상태에서 호출(HomeActivity 진입 시). 멱등 upsert라 반복 호출 안전.
 * - [registerToken]: onNewToken 콜백에서 새 토큰으로 직접 등록.
 * - [unregister]: 로그아웃/회원탈퇴 시 토큰 삭제 "전"에 호출(인증 헤더 필요).
 */
object FcmDeviceManager {

    private const val PLATFORM_ANDROID = "android"

    /** 현재 FCM 등록 토큰 조회 */
    private suspend fun currentToken(): String =
        suspendCancellableCoroutine { cont ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    /** 현재 토큰을 조회해 서버에 등록 (앱/홈 진입 시) */
    suspend fun register() {
        runCatching { currentToken() }
            .onSuccess { registerToken(it) }
            .onFailure { BoatLog.e("[FCM] 토큰 조회 실패 — 등록 생략", it) }
    }

    /** 주어진 토큰을 서버에 등록(멱등 upsert). 성공 시 로컬 캐시에 저장. */
    suspend fun registerToken(token: String) {
        runCatching {
            BoatLog.i("[FCM] 디바이스 등록 시도 — token(len=${token.length}, prefix=${token.take(16)}…)")
            val response = ApiClient.notificationApiService.registerDevice(
                RegisterDeviceRequest(token = token, platform = PLATFORM_ANDROID)
            )
            if (response.isSuccessful) {
                ApiClient.fcmDeviceStore.saveRegisteredToken(token)
                BoatLog.i("[FCM] 디바이스 등록 성공")
            } else {
                BoatLog.e("[FCM] 디바이스 등록 실패 code=${response.code()}")
            }
        }.onFailure { e ->
            BoatLog.e("[FCM] 디바이스 등록 예외", e)
        }
    }

    /**
     * 로그아웃 시 디바이스 해제 — DELETE /notifications/devices/{token} (access token 유효할 때).
     * 마지막으로 등록했던 토큰을 우선 사용하고, 없으면 현재 토큰을 조회한다. best-effort.
     *
     * FCM deleteToken()은 호출하지 않는다 — 서버 DELETE만으로 발송 대상에서 제외되며,
     * 토큰을 삭제하면 재로그인 직후 재발급 전까지 발송이 UNREGISTERED로 실패할 수 있다.
     */
    suspend fun unregister() {
        runCatching {
            val token = ApiClient.fcmDeviceStore.getRegisteredToken() ?: currentToken()
            val response = ApiClient.notificationApiService.unregisterDevice(token)
            if (response.isSuccessful) {
                BoatLog.i("[FCM] 디바이스 해제 성공")
            } else {
                BoatLog.e("[FCM] 디바이스 해제 실패 code=${response.code()}")
            }
        }.onFailure { e ->
            BoatLog.e("[FCM] 디바이스 해제 예외", e)
        }
        runCatching { ApiClient.fcmDeviceStore.clear() }
    }
}
