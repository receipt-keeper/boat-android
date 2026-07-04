package com.windrr.boat.data.remote

import com.windrr.boat.data.remote.model.CreateReceiptRequest
import com.windrr.boat.data.remote.model.CreateReceiptResponse
import com.windrr.boat.data.remote.model.DeleteReceiptResponse
import com.windrr.boat.data.remote.model.ReceiptDetailResponse
import com.windrr.boat.data.remote.model.ReceiptListResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReceiptApiService {

    /**
     * 영수증 등록 — OCR 결과 수정값 또는 수동 입력값으로 영수증 생성.
     * 파일은 먼저 업로드(POST /files)해 받은 fileId들을 receiptFileIds로 전달한다.
     */
    @POST("api/v1/receipts")
    suspend fun createReceipt(@Body request: CreateReceiptRequest): CreateReceiptResponse

    /**
     * 영수증 삭제 — 등록된 영수증과 제품 조회 데이터를 삭제한다.
     * 연결 해제된 파일의 실제 스토리지 삭제는 서버의 파일 정리 작업에서 처리한다.
     */
    @DELETE("api/v1/receipts/{receipt_id}")
    suspend fun deleteReceipt(@Path("receipt_id") receiptId: String): DeleteReceiptResponse

    /** 영수증 상세 조회 — 제품명/구매일/무상 AS 기간/메모/첨부 이미지 등 전체 필드 반환. */
    @GET("api/v1/receipts/{receipt_id}")
    suspend fun getReceiptDetail(@Path("receipt_id") receiptId: String): ReceiptDetailResponse

    /**
     * 영수증 목록 조회 (커서 기반 페이징)
     *
     * @param status  "all" | "active" | "expiring" | "expired"
     * @param sort    "recent" | "expiresOn" | "purchaseDate"
     * @param limit   최대 20
     * @param cursor  다음 페이지 커서 (첫 조회 시 null)
     * @param category 카테고리 완전 일치 필터 (null이면 전체)
     * @param q       검색어 — 제품명/브랜드/구매처/메모 (null이면 전체)
     */
    @GET("api/v1/receipts")
    suspend fun getReceipts(
        @Query("status")   status:   String  = "all",
        @Query("sort")     sort:     String  = "recent",
        @Query("limit")    limit:    Int     = 20,
        @Query("cursor")   cursor:   String? = null,
        @Query("category") category: String? = null,
        @Query("q")        q:        String? = null,
    ): ReceiptListResponse
}
