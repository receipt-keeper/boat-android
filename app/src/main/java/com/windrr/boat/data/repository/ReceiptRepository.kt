package com.windrr.boat.data.repository

import com.windrr.boat.data.local.db.ReceiptDao
import com.windrr.boat.data.local.db.toEntity
import com.windrr.boat.data.local.db.toItem
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.model.CreateReceiptRequest
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.data.remote.model.ReceiptListData
import com.windrr.boat.data.remote.model.ReceiptPagination
import com.windrr.boat.data.remote.model.UpdateReceiptRequest
import okhttp3.MultipartBody

/**
 * 영수증 데이터 접근 — 원격(API) + 로컬(Room) 캐시.
 * - 목록 조회: 성공 시 로컬 캐시에 반영, 실패(오프라인) 시 로컬 캐시로 폴백.
 * - 등록: 파일 업로드 → 영수증 생성 → 로컬 캐시에 저장.
 */
class ReceiptRepository(
    private val dao: ReceiptDao = ApiClient.receiptDao,
) {

    private val receiptApi = ApiClient.receiptApiService
    private val fileApi = ApiClient.fileApiService

    /**
     * 목록 조회. 온라인이면 서버 결과를 로컬 캐시에 반영 후 반환하고,
     * 실패하면 로컬 캐시에서 동일 조건으로 필터링해 반환한다(오프라인 지원).
     */
    suspend fun getReceipts(
        status: String = "all",
        sort: String = "recent",
        limit: Int = 20,
        cursor: String? = null,
        category: String? = null,
        q: String? = null,
    ): Result<ReceiptListData> {
        return runCatching {
            val data = receiptApi.getReceipts(
                status = status, sort = sort, limit = limit,
                cursor = cursor, category = category, q = q,
            ).data
            // 서버 결과를 로컬 캐시에 반영 (첫 페이지 기준)
            if (cursor == null) {
                dao.upsertAll(data.receipts.map { it.toEntity() })
            }
            data.excludeAlreadyExpiredFromExpiring(status)
        }.recoverCatching { error ->
            // 오프라인 폴백 — 로컬 캐시에서 필터/정렬
            val cached = dao.getAll().map { it.toItem() }
            if (cached.isEmpty()) throw error   // 캐시도 없으면 원래 에러 전파
            val filtered = cached
                .filterByStatus(status)
                .filterByCategory(category)
                .filterByQuery(q)
                .sortedBy(sort)
            ReceiptListData(
                receipts = filtered,
                totalCount = filtered.size,
                pagination = ReceiptPagination(
                    hasNext = false,
                    limit = limit,
                    nextCursor = null,
                    totalCount = filtered.size,
                ),
            )
        }
    }

    /** 파일 업로드 후 fileId 목록 반환 */
    suspend fun uploadFiles(parts: List<MultipartBody.Part>): Result<List<String>> = runCatching {
        if (parts.isEmpty()) return@runCatching emptyList()
        fileApi.uploadFiles(parts).data.files.map { it.fileId }
    }

    /** 영수증 등록 후 로컬 캐시에 저장 */
    suspend fun createReceipt(request: CreateReceiptRequest): Result<ReceiptItem> = runCatching {
        val item = receiptApi.createReceipt(request).data
        dao.upsert(item.toEntity())
        item
    }

    /** 영수증 삭제 — 서버 삭제 성공 시에만 로컬 캐시에서도 제거 */
    suspend fun deleteReceipt(receiptId: String): Result<Unit> = runCatching {
        receiptApi.deleteReceipt(receiptId)
        dao.deleteById(receiptId)
    }

    /** 영수증 수정 후 로컬 캐시 갱신 */
    suspend fun updateReceipt(receiptId: String, request: UpdateReceiptRequest): Result<ReceiptItem> = runCatching {
        val item = receiptApi.updateReceipt(receiptId, request).data
        dao.upsert(item.toEntity())
        item
    }

    /**
     * 영수증 상세 조회. 온라인이면 서버 결과를 로컬 캐시에 반영 후 반환하고,
     * 실패(오프라인)하면 로컬 캐시에서 단건 반환한다.
     */
    suspend fun getReceiptDetail(receiptId: String): Result<ReceiptItem> {
        return runCatching {
            val item = receiptApi.getReceiptDetail(receiptId).data
            dao.upsert(item.toEntity())
            item
        }.recoverCatching { error ->
            dao.getById(receiptId)?.toItem() ?: throw error
        }
    }
}

/**
 * 서버가 status="expiring"으로 응답해도 이미 만료(warrantyDDay가 null이거나 음수)된
 * 항목이 섞여 내려올 수 있어 방어적으로 재필터링한다. "만료"는 별도 탭에서 다뤄야 하므로
 * "만료 예정" 리스트/카운트 어디에도 노출되면 안 된다. 단, warrantyDDay == 0(만료일 당일,
 * D-Day)은 아직 만료된 게 아니라 만료 예정에 포함해야 한다. totalCount(홈 화면 배지 등에서
 * 사용)도 제외된 만큼 함께 보정한다.
 */
private fun ReceiptListData.excludeAlreadyExpiredFromExpiring(status: String): ReceiptListData {
    if (status != "expiring") return this
    val filtered = receipts.filter { it.warrantyDDay != null && it.warrantyDDay >= 0 }
    val removed = receipts.size - filtered.size
    if (removed == 0) return this
    return copy(
        receipts = filtered,
        totalCount = (totalCount - removed).coerceAtLeast(0),
        pagination = pagination.copy(totalCount = (pagination.totalCount - removed).coerceAtLeast(0)),
    )
}

// ── 오프라인 로컬 필터/정렬 ─────────────────────────────────────────────────────

private fun List<ReceiptItem>.filterByStatus(status: String): List<ReceiptItem> = when (status) {
    // dDay == 0(만료일 당일, D-Day)은 아직 만료 전이므로 만료 예정에 포함
    "expiring" -> filter { it.warrantyDDay != null && it.warrantyDDay in 0..30 }
    "expired"  -> filter { it.warrantyDDay == null || it.warrantyDDay < 0 }
    else       -> this  // "all"
}

private fun List<ReceiptItem>.filterByCategory(category: String?): List<ReceiptItem> =
    if (category.isNullOrBlank()) this 
    else filter { it.category?.trim()?.replace(" ", "")?.lowercase() == category.trim().replace(" ", "").lowercase() }

private fun List<ReceiptItem>.filterByQuery(q: String?): List<ReceiptItem> {
    if (q.isNullOrBlank()) return this
    val kw = q.trim().lowercase()
    return filter { item ->
        listOfNotNull(item.itemName, item.brandName, item.paymentLocation, item.memo)
            .any { it.lowercase().contains(kw) }
    }
}

private fun List<ReceiptItem>.sortedBy(sort: String): List<ReceiptItem> = when (sort) {
    // 제품명 오름차순 (가나다 순)
    "title"        -> sortedBy { it.itemName }
    // 만료일 오름차순 — 값 없으면 맨 뒤로
    "expiresOn"    -> sortedBy { it.expiresOn ?: "9999-99-99" }
    // 구매일 내림차순 — 값 없으면(빈 문자열) 맨 뒤로
    "purchaseDate" -> sortedByDescending { it.paymentDate ?: "" }
    else           -> sortedByDescending { it.registeredAt ?: "" }  // "recent" — 값 없으면 맨 뒤로
}
