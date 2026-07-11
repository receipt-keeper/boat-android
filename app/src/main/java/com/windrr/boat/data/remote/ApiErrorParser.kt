package com.windrr.boat.data.remote

import com.google.gson.Gson
import com.windrr.boat.data.remote.model.ErrorResponse
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * API 실패를 사용자 노출용 메시지로 변환하는 공통 파서.
 *
 * 규칙:
 * - 5xx(서버 오류) 또는 네트워크 연결 실패 → "네트워크 연결상태를 확인해주세요"
 * - 그 외(4xx 등) → 에러 응답 본문의 data.message
 * - 메시지 파싱 불가 → 일반 오류 문구
 */
object ApiErrorParser {

    const val NETWORK_MESSAGE = "네트워크 연결상태를 확인해주세요"
    private const val UNKNOWN_MESSAGE = "오류가 발생했습니다. 잠시 후 다시 시도해주세요"

    private val gson = Gson()

    /** 예외 기반 (직접 반환 타입 API → HttpException, 연결 실패 → IOException) */
    fun message(t: Throwable): String = when (t) {
        is HttpException -> resolve(t.code()) { t.response()?.errorBody()?.string() }
        is IOException -> NETWORK_MESSAGE
        else -> UNKNOWN_MESSAGE
    }

    /** Response<*> 기반 (Response<Unit> 등 비-2xx 응답) */
    fun message(response: Response<*>): String =
        resolve(response.code()) { response.errorBody()?.string() }

    private inline fun resolve(code: Int, body: () -> String?): String {
        if (code >= 500) return NETWORK_MESSAGE
        return parseMessage(body()) ?: UNKNOWN_MESSAGE
    }

    /** 에러 본문에서 data.message 추출 (errors 리스트가 있으면 첫 번째 항목의 message 우선) */
    private fun parseMessage(raw: String?): String? = runCatching {
        if (raw.isNullOrBlank()) return null
        val response = gson.fromJson(raw, ErrorResponse::class.java)
        val fieldError = response?.data?.errors?.firstOrNull()?.message
        fieldError ?: response?.data?.message?.takeIf { it.isNotBlank() }
    }.getOrNull()
}
