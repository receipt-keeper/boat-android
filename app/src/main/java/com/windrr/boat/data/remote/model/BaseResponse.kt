package com.windrr.boat.data.remote.model

/**
 * 팀 API 컨벤션의 공통 Envelope 구조
 *
 * 서버 응답 형식:
 * {
 *   "message": "성공했습니다.",
 *   "data": { ... }
 * }
 *
 * @property message 서버 응답 메시지
 * @property data 실제 응답 데이터 (에러 시 null)
 */
data class BaseResponse<T>(
    val message: String,
    val data: T? = null
)
