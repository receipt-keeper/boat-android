package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.PromotionResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 월간 크레딧 충전 프로모션 API.
 * OCR 분석 불가(canAnalyze=false) 시 월 5회 무료 충전 혜택을 조회/수령한다.
 */
interface PromotionApiService {

    /** 월간 충전 프로모션 조회 — featureKey=ocr, context=recharge 고정. */
    @GET("api/v1/promotions")
    suspend fun getPromotion(
        @Query("featureKey") featureKey: String,
        @Query("context") context: String,
    ): PromotionResponse

    /**
     * 프로모션 수령 — 응답 balance.remainingCount가 지급 후 최신 잔액.
     * [idempotencyKey]는 동일 요청 재시도 시 같은 값을 재사용해 중복 수령을 막는다.
     */
    @POST("api/v1/promotions/{promotionId}/redemptions")
    suspend fun redeemPromotion(
        @Path("promotionId") promotionId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
    ): PromotionResponse
}
