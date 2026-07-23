package com.windrr.boat

import android.app.Application
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.util.DebugLogger
import android.os.Build
import com.google.android.gms.ads.MobileAds
import com.windrr.boat.core.AppLaunchState
import com.windrr.boat.core.crash.CrashReporter
import com.windrr.boat.core.notification.NotificationHelper
import com.windrr.boat.data.remote.ApiClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AppCore : Application(), SingletonImageLoader.Factory {

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

        // 알림 권한 다이얼로그의 "재실행 시에만 노출" 판단 기준 — 이번 프로세스 시작 시점에
        // 이미 로그인 토큰이 있었는지를 1회만 동기 조회해 기록한다(이후 값 변경 없음).
        AppLaunchState.wasLoggedInAtProcessStart = runBlocking {
            ApiClient.tokenDataStore.accessToken.first() != null
        }

        // Crashlytics 크래시 수집 활성화.
        // 현재는 검증을 위해 모든 빌드에서 수집.
        // 운영 단계에서 디버그 크래시를 제외하려면 아래를 !BuildConfig.DEBUG 로 변경.
        CrashReporter.setCollectionEnabled(true)

        NotificationHelper.createChannels(this)

        // Google AdMob 초기화
        MobileAds.initialize(this) {}
    }

    /**
     * Coil의 전역 ImageLoader가 ApiClient의 인증 OkHttpClient를 쓰도록 지정.
     * 영수증 첨부 이미지(contentPath) 등 Authorization 헤더가 필요한 이미지를 AsyncImage로 바로 로드하기 위함.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { ApiClient.okHttpClient }))
                // GIF 지원 추가
                if (Build.VERSION.SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .apply {
                // TODO: 첨부 이미지 로딩 원인 파악되면 제거 — Coil의 fetch/decode 성공·실패를 Logcat(태그 Coil)에 남긴다.
                if (BuildConfig.DEBUG) logger(DebugLogger())
            }
            .build()
    }
}
