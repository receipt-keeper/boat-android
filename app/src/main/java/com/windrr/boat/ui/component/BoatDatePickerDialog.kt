package com.windrr.boat.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorSystemError
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.RoundedLg
import com.windrr.boat.ui.theme.boatDatePickerColors
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val DATE_DIGITS = 8 // yyyyMMdd
private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/** 화면 표시/전달 포맷 */
private const val DISPLAY_PATTERN = "yyyy.MM.dd"

/**
 * 직접 입력 검증 결과.
 *
 * Material3의 기본 입력 모드는 파싱 실패를 "형식 오류" 하나로만 처리해
 * "존재하지 않는 날짜"를 구분하지 못한다. 에러 노출 정책상 세 가지를 각각
 * 다른 문구로 안내해야 하므로 입력 모드를 직접 구현하고 여기서 판정한다.
 */
private sealed interface DateInput {
    data class Valid(val utcMillis: Long) : DateInput
    /** 아직 아무것도 입력하지 않음 — 에러로 보지 않는다(확인만 비활성). */
    data object Empty : DateInput
    /** 8자리를 아직 다 채우지 않음 → 형식 오류 */
    data object Incomplete : DateInput
    /** 월/일이 실제로 존재하지 않는 값 (예: 43월, 34일, 2월 30일) */
    data object NotARealDate : DateInput
    /** 실제 날짜지만 선택 가능한 범위 밖 (미래 날짜, 연도 범위 밖) */
    data object OutOfRange : DateInput
}

/**
 * 구매일 선택 다이얼로그. 달력 선택 + 직접 입력(YYYY/MM/DD)을 지원한다.
 *
 * - 오늘 이후의 날짜는 선택할 수 없다.
 * - 직접 입력 시 형식 오류 / 존재하지 않는 날짜 / 범위 초과를 각각 다른 문구로 안내하고,
 *   에러 상태에서는 확인 버튼을 비활성화한다.
 *
 * @param initialDate 진입 시 채워둘 날짜("yyyy.MM.dd"). 없으면 미선택 상태로 시작.
 * @param onConfirm 확인 시 "yyyy.MM.dd" 형식 문자열 전달.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoatDatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    initialDate: String? = null,
) {
    val todayMillis = remember { System.currentTimeMillis() }
    val currentYear = remember {
        Calendar.getInstance(UTC).apply { timeInMillis = todayMillis }.get(Calendar.YEAR)
    }
    val yearRange = remember(currentYear) { IntRange(1900, currentYear) }

    // rememberDatePickerState는 yearRange 밖의 값을 넣으면 예외를 던지므로, 선택 가능한 값일 때만 넘긴다.
    val initialMillis = remember(initialDate, todayMillis, yearRange) {
        initialDate?.let { parseDisplayDate(it) }?.takeIf { millis ->
            val year = Calendar.getInstance(UTC).apply { timeInMillis = millis }.get(Calendar.YEAR)
            year in yearRange && millis <= todayMillis
        }
    }

    val dpState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        yearRange = yearRange,
        // 💡 오늘 이후의 날짜는 선택할 수 없도록 제한
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis
            override fun isSelectableYear(year: Int): Boolean = year in yearRange
        },
    )

    var isInputMode by remember { mutableStateOf(false) }
    var digits by remember {
        mutableStateOf(initialMillis?.let { formatDigits(it) }.orEmpty())
    }

    val inputState = remember(digits, todayMillis, yearRange) {
        validateDateInput(digits, todayMillis, yearRange)
    }

    val confirmMillis: Long? =
        if (isInputMode) (inputState as? DateInput.Valid)?.utcMillis else dpState.selectedDateMillis
    // 에러 상태에서는 확인(CTA) 비활성화 — 에러 노출 정책
    val canConfirm = confirmMillis != null

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = boatDatePickerColors(),
        confirmButton = {
            TextButton(
                enabled = canConfirm,
                onClick = {
                    confirmMillis?.let { onConfirm(formatDisplayDate(it)) }
                },
            ) {
                Text(
                    stringResource(R.string.common_confirm),
                    color = if (canConfirm) ColorBrandPrimary else ColorGray400,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel), color = ColorGray600)
            }
        },
    ) {
        if (isInputMode) {
            ManualDateInput(
                digits = digits,
                onDigitsChange = { digits = it },
                errorRes = inputState.errorRes(),
                onSwitchToCalendar = {
                    // 입력값이 유효하면 달력에도 반영한 뒤 전환
                    (inputState as? DateInput.Valid)?.let { dpState.selectedDateMillis = it.utcMillis }
                    isInputMode = false
                },
            )
        } else {
            DatePicker(
                state = dpState,
                showModeToggle = false, // 기본 토글 대신 직접 만든 토글을 headline에 둔다
                colors = boatDatePickerColors(),
                headline = {
                    DatePickerHeadlineRow(
                        text = dpState.selectedDateMillis?.let { formatHeadline(it) }
                            ?: stringResource(R.string.date_picker_headline_empty),
                        icon = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.date_picker_switch_to_input),
                        onToggle = {
                            digits = dpState.selectedDateMillis?.let { formatDigits(it) }.orEmpty()
                            isInputMode = true
                        },
                    )
                },
            )
        }
    }
}

/** 달력/입력 모드 공통 헤드라인 — 우측에 모드 전환 아이콘 */
@Composable
private fun DatePickerHeadlineRow(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = ColorBrandPrimary,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = ColorBrandPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** 직접 입력 모드 본문 */
@Composable
private fun ManualDateInput(
    digits: String,
    onDigitsChange: (String) -> Unit,
    errorRes: Int?,
    onSwitchToCalendar: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.date_picker_title),
            fontSize = 14.sp,
            color = ColorGray600,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )
        DatePickerHeadlineRow(
            text = stringResource(R.string.date_picker_headline_input),
            icon = Icons.Default.DateRange,
            contentDescription = stringResource(R.string.date_picker_switch_to_calendar),
            onToggle = onSwitchToCalendar,
        )

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            OutlinedTextField(
                value = digits,
                onValueChange = { raw ->
                    onDigitsChange(raw.filter(Char::isDigit).take(DATE_DIGITS))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                isError = errorRes != null,
                label = { Text(stringResource(R.string.date_picker_input_label)) },
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = ColorGray900),
                shape = RoundedLg,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = DateSlashVisualTransformation,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorBrandPrimary,
                    unfocusedBorderColor = ColorGray300,
                    errorBorderColor = ColorSystemError,
                    focusedContainerColor = ColorWhite,
                    unfocusedContainerColor = ColorWhite,
                    errorContainerColor = ColorWhite,
                    cursorColor = ColorBrandPrimary,
                    focusedLabelColor = ColorBrandPrimary,
                    unfocusedLabelColor = ColorGray600,
                    errorLabelColor = ColorSystemError,
                    focusedTextColor = ColorGray900,
                    unfocusedTextColor = ColorGray900,
                ),
            )
            if (errorRes != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(errorRes),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorSystemError,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ── 검증 ──────────────────────────────────────────────────────────────────────

private fun DateInput.errorRes(): Int? = when (this) {
    is DateInput.Valid -> null
    DateInput.Empty -> null // 입력 전에는 에러를 노출하지 않는다
    DateInput.Incomplete -> R.string.date_input_error_format
    DateInput.NotARealDate -> R.string.date_input_error_invalid
    DateInput.OutOfRange -> R.string.date_input_error_out_of_range
}

/**
 * 정책 우선순위대로 판정한다: 형식 → 존재하지 않는 날짜 → 범위 초과.
 * 예) "34344334"(3434/43/34)는 43월·34일이라 "존재하지 않는 날짜"로 잡힌다.
 */
private fun validateDateInput(digits: String, todayMillis: Long, yearRange: IntRange): DateInput {
    if (digits.isEmpty()) return DateInput.Empty
    if (digits.length < DATE_DIGITS) return DateInput.Incomplete

    val year = digits.substring(0, 4).toIntOrNull() ?: return DateInput.Incomplete
    val month = digits.substring(4, 6).toIntOrNull() ?: return DateInput.Incomplete
    val day = digits.substring(6, 8).toIntOrNull() ?: return DateInput.Incomplete

    // 실제 존재하는 날짜인지 (43월/34일, 2월 30일 등을 모두 걸러낸다)
    val millis = realDateMillisOrNull(year, month, day) ?: return DateInput.NotARealDate

    if (year !in yearRange || millis > todayMillis) return DateInput.OutOfRange
    return DateInput.Valid(millis)
}

/** lenient=false로 달력에 실제 존재하는 날짜인지 확인. 존재하지 않으면 null. */
private fun realDateMillisOrNull(year: Int, month: Int, day: Int): Long? = runCatching {
    Calendar.getInstance(UTC).apply {
        isLenient = false
        clear()
        set(year, month - 1, day)
    }.timeInMillis
}.getOrNull()

// ── 포맷 ──────────────────────────────────────────────────────────────────────

private fun displayFormatter() =
    SimpleDateFormat(DISPLAY_PATTERN, Locale.KOREA).apply { timeZone = UTC }

private fun formatDisplayDate(utcMillis: Long): String = displayFormatter().format(Date(utcMillis))

private fun parseDisplayDate(value: String): Long? =
    runCatching { displayFormatter().parse(value)?.time }.getOrNull()

private fun formatDigits(utcMillis: Long): String =
    SimpleDateFormat("yyyyMMdd", Locale.KOREA).apply { timeZone = UTC }.format(Date(utcMillis))

private fun formatHeadline(utcMillis: Long): String =
    SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).apply { timeZone = UTC }.format(Date(utcMillis))

/** 숫자 8자리를 YYYY/MM/DD로 보여준다. (실제 상태값은 숫자만 유지) */
private object DateSlashVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(DATE_DIGITS)
        val formatted = buildString {
            digits.forEachIndexed { index, c ->
                if (index == 4 || index == 6) append('/')
                append(c)
            }
        }
        // 연달아 빠르게 삭제(백스페이스)하면 재조합이 미처 따라잡기 전에 이미 짧아진
        // digits 기준으로는 더 이상 유효하지 않은 오프셋이 들어올 수 있다. else 분기에만
        // coerceAtMost가 있고 앞 두 분기엔 없어서, 그 값이 digits.length를 넘는 채로
        // 그대로 반환되면 Compose가 이를 그 길이의 문자열에 인덱싱하다가
        // StringIndexOutOfBoundsException으로 크래시했다. 모든 분기에 동일하게 clamp한다.
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 3 -> offset
                offset <= 5 -> offset + 1
                else -> offset + 2
            }.coerceIn(0, formatted.length)

            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 4 -> offset
                offset <= 7 -> offset - 1
                else -> offset - 2
            }.coerceIn(0, digits.length)
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
