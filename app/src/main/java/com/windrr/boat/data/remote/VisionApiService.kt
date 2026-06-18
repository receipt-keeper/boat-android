package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.VisionAnnotateRequest
import com.windrr.boat.data.remote.model.VisionAnnotateResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface VisionApiService {

    @POST("v1/images:annotate")
    suspend fun annotate(
        @Query("key") apiKey: String,
        @Body request: VisionAnnotateRequest
    ): VisionAnnotateResponse
}
