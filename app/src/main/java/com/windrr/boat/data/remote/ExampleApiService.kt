package com.windrr.boat.data.remote

import retrofit2.http.GET

interface ExampleApiService {
    @GET("api/v1/example/server-error")
    suspend fun serverError()
}
