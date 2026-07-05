package com.windrr.boat.core.notification

import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.model.RegisterDeviceRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * FCM 디바이스(FID) 등록/해제 관리.
 *
 * 서버는 디바이스를 FCM 등록 토큰이 아닌 FID(Firebase Installation ID)로 식별한다.
 * FID는 토큰과 달리 갱신 콜백(onNewToken 같은 것)이 없으므로, 로그인/앱 진입 시점마다
 * 현재 FID를 조회해 멱등 upsert(PUT)로 등록한다.
 *
 * - [register]: 로그인 상태에서 호출 (HomeActivity 진입 시). 서버가 멱등 처리하므로 매번 호출해도 안전.
 * - [unregister]: 로그아웃/회원탈퇴 시 토큰 삭제 "전"에 호출 (인증 헤더가 필요하므로).
 */
object FcmDeviceManager {

    private const val PLATFORM_ANDROID = "android"

    /** 현재 앱 인스턴스의 FID 조회 */
    private suspend fun currentFid(): String =
        suspendCancellableCoroutine { cont ->
            FirebaseInstallations.getInstance().id
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    /** 현재 FID를 서버에 등록(멱등 upsert). 성공 시 로컬에 등록 FID를 캐시한다. */
    suspend fun register() {
        runCatching {
            val fid = currentFid()
            val response = ApiClient.notificationApiService.registerDevice(
                RegisterDeviceRequest(fid = fid, platform = PLATFORM_ANDROID)
            )
            if (response.isSuccessful) {
                ApiClient.fcmDeviceStore.saveRegisteredFid(fid)
                BoatLog.i("[FCM] 디바이스 등록 성공 (fid len=${fid.length})")
            } else {
                BoatLog.e("[FCM] 디바이스 등록 실패 code=${response.code()}")
            }
        }.onFailure { e ->
            BoatLog.e("[FCM] 디바이스 등록 예외", e)
        }
    }

    /**
     * 로그아웃 시 디바이스 해제. 정책 문서 순서를 따른다:
     *   1) DELETE /notifications/devices/{fid}  — access token이 유효할 때(토큰 삭제 전)
     *   2) FCM deleteToken()                     — 2차 안전망(발송 자체를 끊음)
     * 마지막으로 등록했던 FID를 우선 사용하고, 없으면 현재 FID를 조회한다.
     * 모두 best-effort — 실패해도 로그아웃 흐름을 막지 않는다(서버에 소유권 이전·발송에러·60일 정리 3중 안전망 존재).
     */
    suspend fun unregister() {
        // 1) 서버 해제 (멱등, 소유한 등록만 삭제)
        runCatching {
            val fid = ApiClient.fcmDeviceStore.getRegisteredFid() ?: currentFid()
            val response = ApiClient.notificationApiService.unregisterDevice(fid)
            if (response.isSuccessful) {
                BoatLog.i("[FCM] 디바이스 해제 성공")
            } else {
                BoatLog.e("[FCM] 디바이스 해제 실패 code=${response.code()}")
            }
        }.onFailure { e ->
            BoatLog.e("[FCM] 디바이스 해제 예외", e)
        }
        // 2) FCM 토큰 삭제 — 2차 안전망 (DELETE가 실패해도 이 기기로의 발송을 끊는다)
        runCatching {
            suspendCancellableCoroutine<Void?> { cont ->
                FirebaseMessaging.getInstance().deleteToken()
                    .addOnSuccessListener { cont.resume(null) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            BoatLog.i("[FCM] deleteToken 완료 (2차 안전망)")
        }.onFailure { e ->
            BoatLog.e("[FCM] deleteToken 실패", e)
        }
        // 로컬 캐시 정리
        runCatching { ApiClient.fcmDeviceStore.clear() }
    }
}
