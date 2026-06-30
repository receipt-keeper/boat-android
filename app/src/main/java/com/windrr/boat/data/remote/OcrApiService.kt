package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.OcrResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OcrApiService {
    /**
     * 영수증 OCR 분석. 파일을 저장하지 않고 분석 결과만 반환한다.
     * multipart form-data field 명은 "file".
     */
    @Multipart
    @POST("api/v1/ocr")
    suspend fun analyze(
        @Part file: List<MultipartBody.Part>,
    ): OcrResponse
}
