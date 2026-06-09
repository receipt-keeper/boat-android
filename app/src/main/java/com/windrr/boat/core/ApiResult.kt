package com.windrr.boat.core

/**
 * 모든 API 응답을 감싸는 공통 결과 타입
 *
 * ViewModel의 State에서 이렇게 사용:
 *   val receiptList: ApiResult<List<Receipt>> = ApiResult.Idle
 *
 * View에서 when으로 분기:
 *   when (state.receiptList) {
 *       is ApiResult.Loading -> 로딩 스피너
 *       is ApiResult.Success -> 데이터 표시
 *       is ApiResult.Error   -> 에러 메시지
 *       is ApiResult.Idle    -> 아무것도 안 함
 *   }
 */
sealed class ApiResult<out T> {

    /** 아직 요청하지 않은 초기 상태 */
    data object Idle : ApiResult<Nothing>()

    /** 요청 중 */
    data object Loading : ApiResult<Nothing>()

    /**
     * 성공 — 서버 응답 data 필드
     *
     * @property data 서버로부터 받은 실제 응답 데이터
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * 실패 — HTTP 에러 코드 + 서버 메시지
     *
     * @property code HTTP 상태 코드 (네트워크 오류 시 -1)
     * @property message 서버 에러 메시지 또는 로컬 에러 메시지
     */
    data class Error(
        val code: Int,
        val message: String
    ) : ApiResult<Nothing>()
}

/** Success 여부를 간편하게 확인 */
val <T> ApiResult<T>.isSuccess: Boolean get() = this is ApiResult.Success

/** Success일 때 데이터를 꺼내거나 null 반환 */
fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data
