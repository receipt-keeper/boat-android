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
 * - 상태 코드와 무관하게(400/500 등) 서버가 준 data.message가 유효하면 그대로 노출
 * - 메시지가 없거나 비어 있을 때만 앱 기본 문구로 대체 (5xx → 네트워크 문구 / 그 외 → 일반 오류 문구)
 * - 연결 실패(IOException) → 네트워크 문구 / 타임아웃 → 타임아웃 문구
 *
 * 단, 특정 code·상태코드로 전용 UI를 띄우는 곳(OCR 실패 시트, 409 중복 수령 등)은
 * 호출부에서 별도 분기하므로 이 파서를 거치지 않는다.
 */
object ApiErrorParser {

    const val NETWORK_MESSAGE = "네트워크 연결상태를 확인해주세요"
    const val TIMEOUT_MESSAGE = "요청 시간이 초과되었습니다. 다시 시도해 주세요."
    private const val UNKNOWN_MESSAGE = "오류가 발생했습니다. 잠시 후 다시 시도해주세요"

    private val gson = Gson()

    /** 에러 응답 객체 직접 반환 (특수 필드 접근용) */
    fun parse(t: Throwable): ErrorResponse? = runCatching {
        if (t is HttpException) {
            val raw = t.response()?.errorBody()?.string()
            if (raw.isNullOrBlank()) return null
            gson.fromJson(raw, ErrorResponse::class.java)
        } else null
    }.getOrNull()

    /** 예외 기반 (직접 반환 타입 API → HttpException, 연결 실패 → IOException) */
    fun message(t: Throwable): String = when (t) {
        is HttpException -> resolve(t.code()) { t.response()?.errorBody()?.string() }
        is java.net.SocketTimeoutException -> TIMEOUT_MESSAGE
        is IOException -> NETWORK_MESSAGE
        else -> UNKNOWN_MESSAGE
    }

    /** Response<*> 기반 (Response<Unit> 등 비-2xx 응답) */
    fun message(response: Response<*>): String =
        resolve(response.code()) { response.errorBody()?.string() }

    /**
     * 상태 코드와 무관하게 서버가 준 메시지를 우선 사용한다(400이든 500이든).
     * 메시지가 없거나 비어 있을 때만 앱 기본 문구로 대체한다.
     */
    private inline fun resolve(code: Int, body: () -> String?): String =
        parseMessage(body()) ?: if (code >= 500) NETWORK_MESSAGE else UNKNOWN_MESSAGE

    /** 에러 본문에서 data.message 추출 (errors 리스트가 있으면 첫 번째 항목의 message 우선) */
    private fun parseMessage(raw: String?): String? = runCatching {
        if (raw.isNullOrBlank()) return null
        val response = gson.fromJson(raw, ErrorResponse::class.java)
        val fieldError = response?.data?.errors?.firstOrNull()?.message
        fieldError ?: response?.data?.message?.takeIf { it.isNotBlank() }
    }.getOrNull()
}
