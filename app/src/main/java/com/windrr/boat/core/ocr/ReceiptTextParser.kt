package com.windrr.boat.core.ocr

/**
 * Vision API가 반환한 영수증 전문(fullText)에서 5개 필드를 추출하는 파서.
 *
 * - 키워드("제품명:", "보증기간" 등)가 있으면 해당 줄/다음 줄 우선 탐색
 * - 키워드 없으면 정규식 패턴으로 fallback
 * - 인식 불가 필드는 null 반환 → UI에서 수동 입력 유도
 *
 * Gemini 백엔드 연동 후에는 이 파서를 대체할 예정.
 */
internal object ReceiptTextParser {

    fun parse(fullText: String): OcrResult {
        val lines = fullText.lines().map { it.trim() }.filter { it.isNotBlank() }
        return OcrResult(
            productName = extractByKeyword(lines, PRODUCT_KEYWORDS),
            brand = extractByKeyword(lines, BRAND_KEYWORDS),
            price = extractPrice(lines),
            purchaseDateIso = extractDate(lines),
            warrantyMonths = extractWarrantyMonths(lines)
        )
    }

    // ── 제품명 ────────────────────────────────────────────────────────────────

    private val PRODUCT_KEYWORDS = listOf("제품명", "품명", "모델명", "상품명", "품목")

    // ── 브랜드 ────────────────────────────────────────────────────────────────

    private val BRAND_KEYWORDS = listOf("브랜드", "제조사", "제조원", "메이커")

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
        // fallback: 가장 큰 금액 (1,000원 이상)
        return lines.mapNotNull { parseAmount(it) }.filter { it >= 1_000 }.maxOrNull()
    }

    private fun parseAmount(line: String): Long? =
        AMOUNT_REGEX.find(line)
            ?.groupValues?.get(1)
            ?.replace(",", "")
            ?.toLongOrNull()

    // ── 날짜 ──────────────────────────────────────────────────────────────────

    private val DATE_KEYWORDS = listOf("구매일", "구입일", "판매일", "거래일", "발행일", "영수증일")
    private val DATE_4Y = Regex("""(\d{4})[.\-/](\d{1,2})[.\-/](\d{1,2})""")
    private val DATE_2Y = Regex("""(\d{2})[.\-/](\d{1,2})[.\-/](\d{1,2})""")

    private fun extractDate(lines: List<String>): String? {
        for (line in lines) {
            if (DATE_KEYWORDS.any { line.contains(it) }) {
                parseDate(line)?.let { return it }
            }
        }
        for (line in lines) {
            parseDate(line)?.let { return it }
        }
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
        "보증기간", "무상보증", "품질보증", "보증 기간", "무상 보증", "A/S기간", "AS기간"
    )
    private val WARRANTY_YEAR = Regex("""(\d+)\s*년""")
    private val WARRANTY_MONTH = Regex("""(\d+)\s*개?월""")

    private fun extractWarrantyMonths(lines: List<String>): Int? {
        for ((idx, line) in lines.withIndex()) {
            if (WARRANTY_KEYWORDS.any { line.contains(it) }) {
                parseWarrantyMonths(line)?.let { return it }
                // 값이 다음 줄에 있는 경우
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

    // ── 공통: 키워드 뒤 값 추출 ──────────────────────────────────────────────

    // "제품명: 갤럭시 S24"  →  "갤럭시 S24"
    // "제품명\n갤럭시 S24"  →  "갤럭시 S24" (다음 줄)
    private fun extractByKeyword(lines: List<String>, keywords: List<String>): String? {
        for ((idx, line) in lines.withIndex()) {
            val keyword = keywords.firstOrNull { line.contains(it) } ?: continue
            val afterKeyword = line.substringAfter(keyword).trimStart(':', '：', ' ', '\t')
            if (afterKeyword.isNotBlank()) return afterKeyword.trim()
            if (idx + 1 < lines.size) return lines[idx + 1].trim()
        }
        return null
    }
}
