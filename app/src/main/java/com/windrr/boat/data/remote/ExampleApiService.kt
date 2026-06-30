package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.BaseResponse
import retrofit2.http.GET

/** 서버 동작 테스트용 임시 엔드포인트. PM 검증 후 제거 예정. */
interface ExampleApiService {
    @GET("api/v1/example/server-error")
    suspend fun serverError(): BaseResponse<Unit>
}
