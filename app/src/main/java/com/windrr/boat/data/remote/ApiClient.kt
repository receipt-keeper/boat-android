package com.windrr.boat.data.remote

import android.content.Context
import com.windrr.boat.BuildConfig
import com.windrr.boat.data.local.TokenDataStore
import com.windrr.boat.data.local.UserDataStore
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

    const val BASE_URL_PROD  = "https://boatlab-dev.luigi99.cloud/"
    const val BASE_URL_LOCAL = "http://localhost:8000/"

    /** DEBUG 빌드에서 SharedPreferences의 use_local_url 키로 URL 전환 가능 */
    internal const val DEBUG_PREFS      = "debug_prefs"
    internal const val KEY_USE_LOCAL_URL = "use_local_url"

    private const val TIMEOUT_SECONDS = 30L

    private lateinit var appContext: Context

    /**
     * ApplicationContext만 저장 — Activity Context를 저장하지 않으므로 StaticFieldLeak 없음
     * TokenDataStore는 lazy로 appContext에서 생성
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** lazy 빌드 시점에 사용할 baseUrl — DEBUG 빌드에서만 로컬 URL 선택 가능 */
    private fun resolveBaseUrl(): String {
        if (!BuildConfig.DEBUG) return BASE_URL_PROD
        val prefs = appContext.getSharedPreferences(DEBUG_PREFS, Context.MODE_PRIVATE)
        return if (prefs.getBoolean(KEY_USE_LOCAL_URL, false)) BASE_URL_LOCAL else BASE_URL_PROD
    }

    /** TokenDataStore를 외부(Repository 등)에서도 접근할 수 있도록 lazy로 노출 */
    val tokenDataStore: TokenDataStore by lazy { TokenDataStore(appContext) }

    /** UserDataStore를 외부(Repository 등)에서도 접근할 수 있도록 lazy로 노출 */
    val userDataStore: UserDataStore by lazy { UserDataStore(appContext) }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    }

    /**
     * refresh 전용 순수 클라이언트 — TokenInterceptor/Authenticator 미포함.
     * 만료된 AccessToken 주입을 막고, refresh 호출이 또 401일 때 authenticate() 재진입(무한 재귀)을 방지한다.
     */
    private val refreshApiService: AuthApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(resolveBaseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(tokenDataStore))                   // 토큰 헤더 자동 주입
            .authenticator(TokenAuthenticator(tokenDataStore, refreshApiService)) // 401 시 토큰 갱신 후 재시도
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(resolveBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApiService: AuthApiService by lazy { create(AuthApiService::class.java) }

    val userApiService: UserApiService by lazy { create(UserApiService::class.java) }

    val notificationApiService: NotificationApiService by lazy { create(NotificationApiService::class.java) }

    val creditsApiService: CreditsApiService by lazy { create(CreditsApiService::class.java) }

    val usageApiService: UsageApiService by lazy { create(UsageApiService::class.java) }

    val fileApiService: FileApiService by lazy { create(FileApiService::class.java) }

    val receiptApiService: ReceiptApiService by lazy { create(ReceiptApiService::class.java) }

    val exampleApiService: ExampleApiService by lazy { create(ExampleApiService::class.java) }

    /**
     * 새 API Service 인스턴스 생성
     * 예: ApiClient.create(ReceiptApiService::class.java)
     */
    fun <T> create(service: Class<T>): T = retrofit.create(service)
}
