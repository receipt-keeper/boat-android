package com.windrr.boat

import android.app.Application
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
    }
}
