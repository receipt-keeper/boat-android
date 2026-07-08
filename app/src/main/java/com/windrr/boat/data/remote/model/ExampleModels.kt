package com.windrr.boat.data.remote.model

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
