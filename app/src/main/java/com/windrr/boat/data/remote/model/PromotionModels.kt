package com.windrr.boat.data.remote.model

/**
 * 월간 크레딧 충전 프로모션 조회/수령 응답.
 * - GET  /api/v1/promotions?featureKey=ocr&context=recharge  (조회)
 * - POST /api/v1/promotions/{promotionId}/redemptions        (수령)
 *
 * 두 API 응답 스키마가 동일하다. 조회 시엔 [PromotionData.balance]가 null이고,
 * 수령 성공 시엔 balance에 지급 후 잔액이 채워진다.
 */
data class PromotionResponse(
    val success: Boolean,
    val status: Int,
    val data: PromotionData,
)

data class PromotionData(
    /** 프로모션 상태 문자열 — [PromotionState]로 변환해 사용. */
    val state: String,
    /** 수령 API에 넘길 프로모션 ID. state=unavailable이면 null. */
    val promotionId: String?,
    val benefit: PromotionBenefit?,
    val redemption: PromotionRedemption?,
    /** 수령 성공 응답에서만 채워지는 지급 후 크레딧 잔액. */
    val balance: PromotionBalance?,
    val bannerImage: PromotionBannerImage?,
) {
    val stateType: PromotionState get() = PromotionState.from(state)
}

data class PromotionBenefit(
    val featureKey: String,
    val amount: Int,
)

data class PromotionRedemption(
    /** 프로모션 전체 기준 남은 수령 수량. 월간 OCR 충전은 제한이 없어 null. */
    val remainingRedemptions: Int?,
)

data class PromotionBalance(
    val totalGrantedCount: Int,
    val remainingCount: Int,
)

data class PromotionBannerImage(
    val imageUrl: String,
)

/** 프로모션 상태 — 서버 문자열을 앱 분기용으로 매핑. */
enum class PromotionState {
    /** 수령 가능 — 충전 버튼 노출/활성화. */
    REDEEMABLE,
    /** 이번 달 이미 수령함 — 충전 버튼 비노출/비활성화. */
    ALREADY_REDEEMED,
    /** 노출할 혜택 없음. */
    UNAVAILABLE,
    /** 만료된 혜택. */
    EXPIRED,
    /** 수량 소진. */
    EXHAUSTED,
    /** 알 수 없는 상태(신규 값 방어). */
    UNKNOWN;

    companion object {
        fun from(raw: String?): PromotionState = when (raw) {
            "redeemable"      -> REDEEMABLE
            "alreadyRedeemed" -> ALREADY_REDEEMED
            "unavailable"     -> UNAVAILABLE
            "expired"         -> EXPIRED
            "exhausted"       -> EXHAUSTED
            else              -> UNKNOWN
        }
    }
}
