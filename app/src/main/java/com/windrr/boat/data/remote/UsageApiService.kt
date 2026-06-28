package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.UsageResponse
import retrofit2.http.GET

interface UsageApiService {
    @GET("api/v1/usage")
    suspend fun getUsage(): UsageResponse
}
