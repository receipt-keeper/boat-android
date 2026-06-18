package com.windrr.boat.data.remote

import com.windrr.boat.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Google Cloud Vision API 전용 Retrofit 인스턴스.
 *
 * - 기존 ApiClient(백엔드 서버)와 별개: 인증 인터셉터 없음, base URL 다름
 * - API 키는 쿼리 파라미터로 전달 (VisionApiService의 @Query("key"))
 */
object VisionApiClient {

    private const val BASE_URL = "https://vision.googleapis.com/"

    val service: VisionApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(buildOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VisionApiService::class.java)
    }

    private fun buildOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
