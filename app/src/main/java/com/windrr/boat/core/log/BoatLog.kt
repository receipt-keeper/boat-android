package com.windrr.boat.core.log

import android.util.Log
import com.windrr.boat.BuildConfig
import com.windrr.boat.core.crash.CrashReporter

/**
 * 앱 전역 로깅 파사드.
 *
 * 한 번의 함수 호출로 **Logcat + Crashlytics**를 동시에 처리한다.
 * - debug 빌드: Logcat에 출력 (개발 중 확인)
 * - 모든 빌드: Crashlytics breadcrumb 누적 → 크래시 발생 시 직전 로그 추적
 * - 에러 로그에 throwable을 넘기면 non-fatal 예외로 Crashlytics에 기록
 *
 * 사용 예:
 * ```
 * BoatLog.d("갤러리 화면 진입")
 * BoatLog.i("로그인 성공")
 * BoatLog.e("로그인 실패", e)       // 예외까지 Crashlytics에 기록
 * BoatLog.setUser(uid)              // 크래시가 어떤 사용자에게서 났는지 추적
 * BoatLog.setKey("screen", "home")
 * ```
 */
object BoatLog {

    private const val DEFAULT_TAG = "BOAT"

    /**
     * 개발용 상세 로그. Logcat에만 출력(디버그 빌드), Crashlytics에는 남기지 않음.
     *
     * @param message 로그 메시지
     * @param tag Logcat 태그 (기본값 "BOAT")
     */
    fun d(message: String, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    /**
     * 정보 로그. Logcat + Crashlytics breadcrumb.
     *
     * @param message 로그 메시지
     * @param tag Logcat 태그
     */
    fun i(message: String, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) Log.i(tag, message)
        CrashReporter.log("[$tag] $message")
    }

    /**
     * 경고 로그. Logcat + Crashlytics breadcrumb.
     *
     * @param message 로그 메시지
     * @param throwable 함께 출력할 예외 (선택)
     * @param tag Logcat 태그
     */
    fun w(message: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) Log.w(tag, message, throwable)
        CrashReporter.log("[$tag] WARN: $message")
    }

    /**
     * 에러 로그. Logcat + Crashlytics breadcrumb.
     * throwable이 있으면 **non-fatal 예외**로 Crashlytics에 기록한다.
     *
     * @param message 로그 메시지
     * @param throwable 기록할 예외 (선택)
     * @param tag Logcat 태그
     */
    fun e(message: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable)
        CrashReporter.log("[$tag] ERROR: $message")
        throwable?.let { CrashReporter.recordException(it) }
    }

    /**
     * 크래시 추적용 사용자 식별자 설정. 개인정보 대신 내부 ID(예: Firebase UID) 권장.
     *
     * @param id 사용자 식별자
     */
    fun setUser(id: String) {
        CrashReporter.setUserId(id)
    }

    /** 로그아웃 등으로 사용자 식별자 해제. */
    fun clearUser() {
        CrashReporter.setUserId("")
    }

    /**
     * 크래시 발생 당시 상태 추적용 커스텀 키-값 설정.
     *
     * @param key 키
     * @param value 값
     */
    fun setKey(key: String, value: String) {
        CrashReporter.setCustomKey(key, value)
    }
}
