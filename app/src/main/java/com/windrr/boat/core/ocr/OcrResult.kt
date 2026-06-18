package com.windrr.boat.core.ocr

/**
 * Vision API OCR 파싱 결과. 인식 실패 필드는 null.
 *
 * @param productName   제품명
 * @param brand         브랜드/제조사
 * @param price         구매 가격 (원 단위)
 * @param purchaseDateIso 구매일 (ISO 8601: "YYYY-MM-DD")
 * @param warrantyMonths  무상 AS 기간 (개월)
 */
data class OcrResult(
    val productName: String? = null,
    val brand: String? = null,
    val price: Long? = null,
    val purchaseDateIso: String? = null,
    val warrantyMonths: Int? = null
) {
    val isEmpty: Boolean
        get() = productName == null && brand == null && price == null
                && purchaseDateIso == null && warrantyMonths == null
}
