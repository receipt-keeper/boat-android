package com.windrr.boat.data.remote

import android.content.Context
import com.windrr.boat.BuildConfig
import com.windrr.boat.data.local.TokenDataStore
import com.windrr.boat.data.remote.interceptor.TokenAuthenticator
import com.windrr.boat.data.remote.interceptor.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit + OkHttp 싱글톤
 *
 * 사용법:
 *   1. AppCore.onCreate()에서 ApiClient.init(this) 호출
 *   2. 새 API 추가 시: ApiClient.create(XxxApiService::class.java)
 */
object ApiClient {

    private const val BASE_URL = "https://boatlab-dev.luigi99.cloud/"
    private const val TIMEOUT_SECONDS = 30L

    private lateinit var appContext: Context

    /**
     * ApplicationContext만 저장 — Activity Context를 저장하지 않으므로 StaticFieldLeak 없음
     * TokenDataStore는 lazy로 appContext에서 생성
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** TokenDataStore를 외부(Repository 등)에서도 접근할 수 있도록 lazy로 노출 */
    val tokenDataStore: TokenDataStore by lazy { TokenDataStore(appContext) }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(tokenDataStore))   // 토큰 헤더 자동 주입
            .authenticator(TokenAuthenticator(tokenDataStore))  // 401 시 토큰 갱신
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApiService: AuthApiService by lazy { create(AuthApiService::class.java) }

    /**
     * 새 API Service 인스턴스 생성
     * 예: ApiClient.create(ReceiptApiService::class.java)
     */
    fun <T> create(service: Class<T>): T = retrofit.create(service)
}
