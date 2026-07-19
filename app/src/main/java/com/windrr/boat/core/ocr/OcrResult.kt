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
    OTHER("기타 제품");

    companion object {
        /**
         * 서버 category 문자열 → enum. 공백 편차 + 라벨 변형(IT 기기/영상 IT 제품 등)을 흡수.
         * displayName과의 완전 일치만 보다가, 서버가 "IT 기기"를 내려주는 경우 "IT 제품"과
         * 매칭 실패 → 주방가전으로 조용히 폴백되던 버그의 원인. iOS DeviceCategory.from(serverValue:) 대응.
         */
        fun from(serverValue: String?): DeviceCategory? {
            val key = normalizeCategory(serverValue)
            if (key.isEmpty()) return null
            return when (key) {
                "주방가전" -> KITCHEN
                "세탁/청소", "세탁청소" -> LAUNDRY
                "리빙/냉난방", "리빙냉난방" -> LIVING
                "it제품", "it기기", "영상/it제품", "영상it제품" -> IT
                "기타", "기타제품", "기타기기" -> OTHER
                else -> null
            }
        }

        /** 공백 제거 + 소문자화 (슬래시는 카테고리 구분자로 유지) */
        fun normalizeCategory(raw: String?): String =
            (raw ?: "").trim().replace(" ", "").lowercase()
    }
}
