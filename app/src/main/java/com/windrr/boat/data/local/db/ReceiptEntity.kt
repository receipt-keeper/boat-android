package com.windrr.boat.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.windrr.boat.data.remote.model.ReceiptItem

/**
 * 영수증 로컬 캐시 엔티티 — 오프라인에서도 등록한 영수증을 볼 수 있도록 저장.
 * [ReceiptItem]의 필드를 그대로 미러링한다. (imageUrl 등 서버 계산값 포함)
 */
@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey val receiptId: String,
    val itemName: String,
    val brandName: String?,
    val paymentLocation: String?,
    val paymentDate: String?,
    val totalAmount: Int?,
    val periodMonths: Int?,
    val expiresOn: String?,
    val category: String?,
    val subCategory: String?,
    val memo: String?,
    val requiresPhysicalReceipt: Boolean,
    val receiptFileIds: List<String>,
    val imageUrl: String?,
    val warrantyDDay: Int?,
    val serialNumber: String?,
    val supportUrl: String?,
    val registeredAt: String?,
)

fun ReceiptEntity.toItem(): ReceiptItem = ReceiptItem(
    receiptId = receiptId,
    itemName = itemName,
    brandName = brandName,
    paymentLocation = paymentLocation,
    paymentDate = paymentDate,
    totalAmount = totalAmount,
    periodMonths = periodMonths,
    expiresOn = expiresOn,
    category = category,
    subCategory = subCategory,
    memo = memo,
    requiresPhysicalReceipt = requiresPhysicalReceipt,
    receiptFileIds = receiptFileIds,
    imageUrl = imageUrl,
    warrantyDDay = warrantyDDay,
    serialNumber = serialNumber,
    supportUrl = supportUrl,
    registeredAt = registeredAt,
)

fun ReceiptItem.toEntity(): ReceiptEntity = ReceiptEntity(
    receiptId = receiptId,
    itemName = itemName,
    brandName = brandName,
    paymentLocation = paymentLocation,
    paymentDate = paymentDate,
    totalAmount = totalAmount,
    periodMonths = periodMonths,
    expiresOn = expiresOn,
    category = category,
    subCategory = subCategory,
    memo = memo,
    requiresPhysicalReceipt = requiresPhysicalReceipt,
    receiptFileIds = receiptFileIds,
    imageUrl = imageUrl,
    warrantyDDay = warrantyDDay,
    serialNumber = serialNumber,
    supportUrl = supportUrl,
    registeredAt = registeredAt,
)
