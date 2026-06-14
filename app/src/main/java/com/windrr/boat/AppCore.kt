package com.windrr.boat

import android.app.Application
import com.windrr.boat.core.crash.CrashReporter
import com.windrr.boat.data.remote.ApiClient

class AppCore : Application() {

    companion object {
        lateinit var instance: AppCore
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // applicationContext를 ApiClient에 전달
        // TokenDataStore는 ApiClient 내부에서 lazy 생성
        ApiClient.init(this)

        // Crashlytics 크래시 수집 활성화.
        // 현재는 검증을 위해 모든 빌드에서 수집.
        // 운영 단계에서 디버그 크래시를 제외하려면 아래를 !BuildConfig.DEBUG 로 변경.
        CrashReporter.setCollectionEnabled(true)
    }
}
