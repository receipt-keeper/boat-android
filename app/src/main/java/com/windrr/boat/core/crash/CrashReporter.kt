package com.windrr.boat.core.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Firebase Crashlytics 래퍼.
 *
 * 앱의 나머지 코드가 Firebase에 직접 의존하지 않도록 크래시 리포팅을 한 곳에 모음.
 * - 미처리(fatal) 크래시는 Crashlytics가 자동 수집함
 * - 잡힌 예외(non-fatal)는 [recordException]으로 수동 기록
 */
object CrashReporter {

    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    /**
     * 크래시 수집 활성화 여부 설정.
     *
     * 운영 배포 시 디버그 빌드의 크래시가 콘솔을 오염시키지 않게 하려면
     * AppCore에서 `!BuildConfig.DEBUG`로 설정 권장.
     *
     * @param enabled 수집 활성화 여부
     */
    fun setCollectionEnabled(enabled: Boolean) {
        crashlytics.isCrashlyticsCollectionEnabled = enabled
    }

    /**
     * 크래시 발생 시 함께 전송될 로그 메시지 기록.
     *
     * @param message 기록할 로그
     */
    fun log(message: String) {
        crashlytics.log(message)
    }

    /**
     * 잡힌 예외(non-fatal)를 Crashlytics에 기록.
     * try-catch로 처리했지만 추적하고 싶은 오류에 사용.
     *
     * @param throwable 기록할 예외
     */
    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    /**
     * 크래시 리포트에 사용자 식별자 부여 (어떤 사용자에게서 발생했는지 추적).
     * 개인정보(이메일 등) 대신 내부 ID 사용 권장.
     *
     * @param id 사용자 식별자
     */
    fun setUserId(id: String) {
        crashlytics.setUserId(id)
    }

    /**
     * 크래시 리포트에 커스텀 키-값 부여 (발생 당시 상태 추적).
     *
     * @param key 키
     * @param value 값
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * 검증용 강제 크래시. **테스트 전용** — 앱이 즉시 종료되고
     * 다음 실행 시 Crashlytics 콘솔에 리포트가 올라감.
     */
    fun forceTestCrash(): Nothing {
        throw RuntimeException("Test Crash from BOAT (Crashlytics 연동 검증)")
    }
}
