package com.windrr.boat.core.ocr

/**
 * Vision API OCR 파싱 결과. 인식 실패 필드는 null.
 *
 * @param productName     제품명 (브랜드/모델명 패턴 포함)
 * @param brand           브랜드/제조사
 * @param price           구매 가격 (원 단위)
 * @param purchaseDateIso 구매일 (ISO 8601: "YYYY-MM-DD")
 * @param warrantyMonths  무상 AS 기간 (개월)
 * @param serialNumber    시리얼 넘버 (S/N, 일련번호)
 * @param category        대분류 카테고리 (제품명 기반 자동 매핑)
 */
data class OcrResult(
    val productName: String? = null,
    val brand: String? = null,
    val price: Long? = null,
    val purchaseDateIso: String? = null,
    val warrantyMonths: Int? = null,
    val serialNumber: String? = null,
    val category: DeviceCategory = DeviceCategory.OTHER,
    val items: List<String> = emptyList()
) {
    val isEmpty: Boolean
        get() = productName == null && brand == null && price == null
                && purchaseDateIso == null && warrantyMonths == null && serialNumber == null
                && items.isEmpty()
}

enum class DeviceCategory(val displayName: String) {
    KITCHEN("주방 가전"),
    LAUNDRY("세탁/청소"),
    LIVING("리빙/냉난방"),
    IT("IT 제품"),
    OTHER("기타 제품")
}
