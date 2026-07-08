package com.windrr.boat.data.repository

import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.PromotionApiService
import com.windrr.boat.data.remote.model.PromotionData

/**
 * 월간 OCR 충전 프로모션 흐름을 담당하는 Repository.
 *
 * 연동 순서(서버 명세):
 * 1. GET /usage 로 canAnalyze 확인 (호출부에서 수행)
 * 2. canAnalyze=false → [getOcrRechargePromotion] 으로 프로모션 조회
 * 3. state=redeemable → [redeem] 으로 수령
 * 4. 수령 응답의 balance.remainingCount 를 앱 잔여 크레딧으로 반영
 */
class PromotionRepository(
    private val promotionApi: PromotionApiService = ApiClient.promotionApiService,
) {

    /** 월간 OCR 충전 프로모션 조회 — featureKey=ocr, context=recharge 고정. */
    suspend fun getOcrRechargePromotion(): Result<PromotionData> = runCatching {
        promotionApi.getPromotion(featureKey = FEATURE_OCR, context = CONTEXT_RECHARGE).data
    }

    /**
     * 프로모션 수령. [idempotencyKey]는 동일 요청 재시도 시 같은 값을 재사용해야 중복 수령을 막는다.
     * 성공 시 [PromotionData.balance]에 지급 후 잔액이 담겨 온다.
     */
    suspend fun redeem(promotionId: String, idempotencyKey: String): Result<PromotionData> = runCatching {
        promotionApi.redeemPromotion(promotionId, idempotencyKey).data
    }

    companion object {
        const val FEATURE_OCR = "ocr"
        const val CONTEXT_RECHARGE = "recharge"
    }
}
