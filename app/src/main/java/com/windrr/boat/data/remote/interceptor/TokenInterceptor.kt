package com.windrr.boat.data.remote.interceptor

import com.windrr.boat.data.local.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 모든 API 요청에 AccessToken을 자동으로 Authorization 헤더에 주입
 * OkHttp는 동기 환경이므로 runBlocking으로 DataStore Flow를 읽음
 */
class TokenInterceptor(private val tokenDataStore: TokenDataStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenDataStore.accessToken.first() }

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
