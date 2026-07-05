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

/**
 * POST /api/v1/example/push — 로그인 사용자의 등록된 모든 디바이스로 테스트 푸시 즉시 발송.
 * 알림 레코드 미생성, 수신 설정 미확인. 앱 푸시 연동 확인용 임시 API.
 */
data class TestPushRequest(
    val title: String,
    val body: String,
)

data class TestPushResponse(
    val success: Boolean,
    val status: Int,
    val data: TestPushData,
)

data class TestPushData(
    val invalidDeviceCount: Int = 0,
    val targetedDeviceCount: Int = 0,
)
