package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.BaseResponse
import com.windrr.boat.data.remote.model.OcrTestCreditsResponse
import retrofit2.http.GET
import retrofit2.http.POST

/** 서버 동작 테스트용 임시 엔드포인트. PM 검증 후 제거 예정. */
interface ExampleApiService {
    @GET("api/v1/example/server-error")
    suspend fun serverError(): BaseResponse<Unit>

    /**
     * OCR 크레딧 소진 시 테스트를 위해 5회를 임시로 재지급하는 example 모듈 보조 API.
     * TODO: 정식 충전/이벤트 지급 API가 나오면 [com.windrr.boat.feature.receipt.NoTokenBottomSheet]의
     *       "충전하기" 호출을 이 임시 API 대신 정식 API로 교체해야 한다.
     */
    @POST("api/v1/example/ocr-test-credits")
    suspend fun grantOcrTestCredits(): OcrTestCreditsResponse
}
