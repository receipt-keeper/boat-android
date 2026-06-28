package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.FileUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileApiService {
    @Multipart
    @POST("api/v1/files")
    suspend fun uploadFiles(
        @Part files: List<MultipartBody.Part>,
    ): FileUploadResponse
}
