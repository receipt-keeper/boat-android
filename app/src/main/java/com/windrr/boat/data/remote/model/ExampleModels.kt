package com.windrr.boat.data.remote.model

/**
 * POST /api/v1/example/ocr-test-credits 응답.
 * OCR 연동 테스트를 위해 인증된 사용자에게 OCR 크레딧을 임시 지급하는 example 모듈 보조 API.
 * TODO: 정식 충전/이벤트 지급 API가 나오면 이 임시 API 호출부를 교체해야 한다.
 */
data class OcrTestCreditsResponse(
    val success: Boolean,
    val status: Int,
    val data: OcrTestCreditsData,
)

data class OcrTestCreditsData(
    val featureKey: String,
    val reason: String,
    val grantedCount: Int,
)
