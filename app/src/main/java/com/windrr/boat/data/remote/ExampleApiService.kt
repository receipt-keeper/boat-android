package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.BaseResponse
import com.windrr.boat.data.remote.model.TestPushRequest
import com.windrr.boat.data.remote.model.TestPushResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** 서버 동작 테스트용 임시 엔드포인트. PM 검증 후 제거 예정. */
interface ExampleApiService {
    @GET("api/v1/example/server-error")
    suspend fun serverError(): BaseResponse<Unit>

    // OCR 월간 충전은 example API(ocr-test-credits) 대신 정식 프로모션 API로 이관됨:
    //   PromotionApiService.getPromotion / redeemPromotion

    /** 로그인 사용자의 등록된 모든 디바이스로 테스트 푸시 발송 (연동 확인용 임시 API). */
    @POST("api/v1/example/push")
    suspend fun sendTestPush(@Body request: TestPushRequest): TestPushResponse
}
