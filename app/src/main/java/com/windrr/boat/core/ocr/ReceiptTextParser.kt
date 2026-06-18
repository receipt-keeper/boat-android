package com.windrr.boat.core.ocr

/**
 * Vision API가 반환한 영수증 전문(fullText)에서 7개 필드를 추출하는 파서.
 *
 * 추출 방식:
 * - 제품명: 키워드("제품명:", "모델명:" 등) 또는 모델번호 패턴(SM-S928B 등)
 * - 구매일: 날짜 정규식 (2024.06.18 / 24/06/18 등)
 * - 무상 AS 기간: "보증기간 1년", "warranty 12months" 패턴
 * - 브랜드명: 제조사명 키워드 매칭
 * - 가격: 숫자 + 원/₩ 패턴
 * - 시리얼 넘버: S/N, 일련번호 패턴
 * - 대분류: 제품명 기준 카테고리 매핑 (PRD 14종 기준)
 *
 * Gemini 백엔드 연동 후 대체 예정.
 */
internal object ReceiptTextParser {

    fun parse(fullText: String): OcrResult {
        val lines = fullText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val productName = extractProductName(lines)
        return OcrResult(
            productName = productName,
            brand = extractBrand(lines),
            price = extractPrice(lines),
            purchaseDateIso = extractDate(lines),
            warrantyMonths = extractWarrantyMonths(lines),
            serialNumber = extractSerialNumber(lines),
            category = mapCategory(productName)
        )
    }

    // ── 제품명 ────────────────────────────────────────────────────────────────
    // 1순위: 키워드 뒤 값, 2순위: 모델번호 패턴 (영문+숫자 조합 8자 이상)

    private val PRODUCT_KEYWORDS = listOf("제품명", "품명", "모델명", "상품명", "품목", "모델번호")
    private val MODEL_NUMBER_REGEX = Regex("""[A-Z]{1,4}[-_]?[A-Z0-9]{4,}""")  // SM-S928B, WF19T6000KW 등

    private fun extractProductName(lines: List<String>): String? {
        extractByKeyword(lines, PRODUCT_KEYWORDS)?.let { return it }
        // 모델번호 패턴 fallback
        for (line in lines) {
            MODEL_NUMBER_REGEX.find(line)?.let { return it.value }
        }
        return null
    }

    // ── 브랜드 ────────────────────────────────────────────────────────────────

    private val BRAND_KEYWORDS = listOf("브랜드", "제조사", "제조원", "메이커", "제조")

    private fun extractBrand(lines: List<String>): String? =
        extractByKeyword(lines, BRAND_KEYWORDS)

    // ── 가격 ──────────────────────────────────────────────────────────────────

    private val PRICE_KEYWORDS = listOf(
        "결제금액", "판매가", "판매금액", "합계", "합 계", "구매가", "소비자가", "실결제", "금액"
    )
    private val AMOUNT_REGEX = Regex("""[₩￦]?\s*([\d,]{4,})\s*원?""")

    private fun extractPrice(lines: List<String>): Long? {
        for (line in lines) {
            if (PRICE_KEYWORDS.any { line.contains(it) }) {
                parseAmount(line)?.let { return it }
            }
        }
        return lines.mapNotNull { parseAmount(it) }.filter { it >= 1_000 }.maxOrNull()
    }

    private fun parseAmount(line: String): Long? =
        AMOUNT_REGEX.find(line)?.groupValues?.get(1)?.replace(",", "")?.toLongOrNull()

    // ── 날짜 ──────────────────────────────────────────────────────────────────

    private val DATE_KEYWORDS = listOf("구매일", "구입일", "판매일", "거래일", "발행일", "영수증일")
    private val DATE_4Y = Regex("""(\d{4})[.\-/](\d{1,2})[.\-/](\d{1,2})""")
    private val DATE_2Y = Regex("""(\d{2})[.\-/](\d{1,2})[.\-/](\d{1,2})""")

    private fun extractDate(lines: List<String>): String? {
        for (line in lines) {
            if (DATE_KEYWORDS.any { line.contains(it) }) parseDate(line)?.let { return it }
        }
        for (line in lines) { parseDate(line)?.let { return it } }
        return null
    }

    private fun parseDate(line: String): String? {
        DATE_4Y.find(line)?.let { m ->
            val year = m.groupValues[1].toInt()
            if (year in 2000..2035) return toIso(m.groupValues[1], m.groupValues[2], m.groupValues[3])
        }
        DATE_2Y.find(line)?.let { m ->
            return toIso("20${m.groupValues[1]}", m.groupValues[2], m.groupValues[3])
        }
        return null
    }

    private fun toIso(y: String, m: String, d: String) =
        "${y}-${m.padStart(2, '0')}-${d.padStart(2, '0')}"

    // ── 보증기간 ──────────────────────────────────────────────────────────────

    private val WARRANTY_KEYWORDS = listOf(
        "보증기간", "무상보증", "품질보증", "보증 기간", "무상 보증", "A/S기간", "AS기간",
        "warranty", "guarantee"
    )
    private val WARRANTY_YEAR = Regex("""(\d+)\s*(년|year)""", RegexOption.IGNORE_CASE)
    private val WARRANTY_MONTH = Regex("""(\d+)\s*(개?월|month)""", RegexOption.IGNORE_CASE)

    private fun extractWarrantyMonths(lines: List<String>): Int? {
        for ((idx, line) in lines.withIndex()) {
            if (WARRANTY_KEYWORDS.any { line.contains(it, ignoreCase = true) }) {
                parseWarrantyMonths(line)?.let { return it }
                if (idx + 1 < lines.size) parseWarrantyMonths(lines[idx + 1])?.let { return it }
            }
        }
        return null
    }

    private fun parseWarrantyMonths(line: String): Int? {
        WARRANTY_YEAR.find(line)?.let { return (it.groupValues[1].toIntOrNull() ?: return null) * 12 }
        WARRANTY_MONTH.find(line)?.let { return it.groupValues[1].toIntOrNull() }
        return null
    }

    // ── 시리얼 넘버 ───────────────────────────────────────────────────────────

    private val SERIAL_KEYWORDS = listOf("s/n", "s.n", "시리얼", "일련번호", "serial", "serial no")
    private val SERIAL_VALUE_REGEX = Regex("""[A-Z0-9]{6,}""")

    private fun extractSerialNumber(lines: List<String>): String? {
        for (line in lines) {
            val lower = line.lowercase()
            if (SERIAL_KEYWORDS.any { lower.contains(it) }) {
                val keyword = SERIAL_KEYWORDS.firstOrNull { lower.contains(it) } ?: continue
                val afterKeyword = line.substring(lower.indexOf(keyword) + keyword.length)
                    .trimStart(':', '：', ' ', '\t')
                SERIAL_VALUE_REGEX.find(afterKeyword)?.let { return it.value }
                // 다음 줄에 값이 있는 경우
                val idx = lines.indexOf(line)
                if (idx + 1 < lines.size) {
                    SERIAL_VALUE_REGEX.find(lines[idx + 1])?.let { return it.value }
                }
            }
        }
        return null
    }

    // ── 대분류 카테고리 매핑 (PRD 기준) ─────────────────────────────────────

    private val CATEGORY_MAP: Map<DeviceCategory, List<String>> = mapOf(
        DeviceCategory.KITCHEN to listOf("냉장고", "전자레인지", "밥솥", "정수기", "식기세척기", "오븐", "인덕션", "가스레인지"),
        DeviceCategory.LAUNDRY to listOf("세탁기", "건조기", "청소기", "로봇청소기", "스팀청소기"),
        DeviceCategory.LIVING to listOf("에어컨", "선풍기", "공기청정기", "가습기", "제습기", "난방기", "온풍기", "히터"),
        DeviceCategory.IT to listOf(
            "tv", "텔레비전", "모니터", "노트북", "태블릿", "게임기", "카메라",
            "스피커", "이어폰", "헤드폰", "스마트폰", "휴대폰", "pc", "데스크탑",
            "프린터", "스캐너", "라우터", "공유기"
        )
    )

    private fun mapCategory(productName: String?): DeviceCategory {
        if (productName.isNullOrBlank()) return DeviceCategory.OTHER
        val lower = productName.lowercase()
        for ((category, keywords) in CATEGORY_MAP) {
            if (keywords.any { lower.contains(it) }) return category
        }
        return DeviceCategory.OTHER
    }

    // ── 공통: 키워드 뒤 값 추출 ──────────────────────────────────────────────

    private fun extractByKeyword(lines: List<String>, keywords: List<String>): String? {
        for ((idx, line) in lines.withIndex()) {
            val keyword = keywords.firstOrNull { line.contains(it, ignoreCase = true) } ?: continue
            val afterKeyword = line.substringAfter(keyword, "").trimStart(':', '：', ' ', '\t')
            if (afterKeyword.isNotBlank()) return afterKeyword.trim()
            if (idx + 1 < lines.size) return lines[idx + 1].trim()
        }
        return null
    }
}
