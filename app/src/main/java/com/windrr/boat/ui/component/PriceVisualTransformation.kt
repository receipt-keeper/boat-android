package com.windrr.boat.ui.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * 숫자만 입력받는 필드에 실시간으로 천단위 콤마를 표시한다.
 * 상태값(state)은 콤마 없는 순수 숫자 문자열로 유지되고 화면 표시만 변환된다.
 */
class PriceVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = groupByThousands(digits)
        val commaPositions = commaPositionsIn(digits.length)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val commas = commaPositions.count { it <= offset }
                return (offset + commas).coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val commas = commaPositions.count { it < offset }
                return (offset - commas).coerceIn(0, digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }

    // 콤마를 삽입한 문자열 생성 — 숫자 파싱 없이 문자 위치만으로 그룹핑(선행 0도 그대로 보존)
    private fun groupByThousands(digits: String): String {
        if (digits.length <= 3) return digits
        val firstGroupLen = digits.length % 3
        var i = if (firstGroupLen == 0) 3 else firstGroupLen
        val sb = StringBuilder().append(digits, 0, i)
        while (i < digits.length) {
            sb.append(',').append(digits, i, i + 3)
            i += 3
        }
        return sb.toString()
    }

    // 원본 문자열에서 콤마가 삽입되는 digit-index 목록 (offset 매핑에 재사용)
    private fun commaPositionsIn(length: Int): List<Int> {
        if (length <= 3) return emptyList()
        val firstGroupLen = length % 3
        val positions = mutableListOf(if (firstGroupLen == 0) 3 else firstGroupLen)
        while (positions.last() + 3 < length) positions.add(positions.last() + 3)
        return positions
    }
}
