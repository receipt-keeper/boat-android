package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.CreditsResponse
import retrofit2.http.GET

interface CreditsApiService {
    @GET("api/v1/credits")
    suspend fun getCredits(): CreditsResponse
}
