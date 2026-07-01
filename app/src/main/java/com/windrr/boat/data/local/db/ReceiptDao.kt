package com.windrr.boat.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ReceiptDao {

    /** 조회 결과를 로컬 캐시에 반영 (있으면 갱신, 없으면 삽입) */
    @Upsert
    suspend fun upsertAll(items: List<ReceiptEntity>)

    /** 단건 삽입/갱신 — 신규 등록 직후 캐시 반영 */
    @Upsert
    suspend fun upsert(item: ReceiptEntity)

    /** 오프라인 폴백용 — 전체 캐시 반환 */
    @Query("SELECT * FROM receipts")
    suspend fun getAll(): List<ReceiptEntity>

    @Query("DELETE FROM receipts WHERE receiptId = :receiptId")
    suspend fun deleteById(receiptId: String)

    @Query("DELETE FROM receipts")
    suspend fun clear()
}
