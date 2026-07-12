package com.windrr.boat.data.repository

import com.windrr.boat.core.ApiResult
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.remote.model.BaseResponse
import retrofit2.Response
import java.io.IOException

/**
 * 모든 Repository가 상속하는 베이스 클래스
 * apiCall 하나로 에러 처리를 공통화
 *
 * 새 Repository 추가 시:
 *   class ReceiptRepository : BaseRepository() {
 *       suspend fun getReceipts() = apiCall { api.getReceipts() }
 *   }
 */
abstract class BaseRepository {

    /**
     * Retrofit 호출을 ApiResult로 안전하게 감싸는 공통 함수
     * - 네트워크 오류 / 서버 오류 / 빈 응답 모두 처리
     * - Envelope(BaseResponse) 구조에서 data 필드만 추출하여 반환
     */
    protected suspend fun <T> apiCall(
        call: suspend () -> Response<BaseResponse<T>>
    ): ApiResult<T> {
        return try {
            val response = call()

            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data

                if (data != null) {
                    ApiResult.Success(data)
                } else {
                    ApiResult.Error(
                        code = response.code(),
                        message = body?.message ?: "데이터가 없습니다"
                    )
                }
            } else {
                ApiResult.Error(
                    code = response.code(),
                    message = response.errorBody()?.string() ?: "서버 오류가 발생했습니다"
                )
            }

        } catch (e: IOException) {
            // 네트워크 연결 문제 — 실패 사유를 Crashlytics에 non-fatal로 기록
            BoatLog.e("네트워크 통신 실패: ${e.message}", e, tag = "Network")
            ApiResult.Error(code = -1, message = "네트워크 연결을 확인해주세요")
        } catch (e: Exception) {
            // 파싱 오류 등 예외
            BoatLog.e("API 호출 실패: ${e.message}", e, tag = "Network")
            ApiResult.Error(code = -1, message = e.message ?: "알 수 없는 오류가 발생했습니다")
        }
    }
}
