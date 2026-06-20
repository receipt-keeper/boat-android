package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.LoginRequest
import com.windrr.boat.data.remote.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
