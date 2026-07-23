package com.windrr.boat.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorSystemError
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.RoundedLg

private val FieldHeight = 52.dp

/**
 * 공통 입력 필드. (디자인 스펙: height 52dp / radius 8dp)
 * - 라벨(+필수 표시) / placeholder
 * - 상태: 기본(회색 테두리) · 포커스(파랑) · 에러(빨강 + 헬퍼 텍스트)
 *
 * width는 호출부 modifier로 제어(기본 fillMaxWidth). 358dp는 좌우 여백 적용 시의 디자인 폭.
 */
@Composable
fun BoatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    placeholder: String = "",
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier = modifier) {
        if (label != null) {
            Row {
                Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorGray600)
                if (required) {
                    Text(text = " *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorSystemError)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(FieldHeight),
            enabled = enabled,
            singleLine = true,
            isError = isError,
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, color = ColorGray900),
            placeholder = { Text(text = placeholder, color = ColorGray400, fontSize = 16.sp) },
            shape = RoundedLg, // 8dp
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorBrandPrimary,
                unfocusedBorderColor = ColorGray300,
                errorBorderColor = ColorSystemError,
                disabledBorderColor = ColorGray200,
                focusedContainerColor = ColorWhite,
                unfocusedContainerColor = ColorWhite,
                errorContainerColor = ColorWhite,
                disabledContainerColor = ColorWhite,
                cursorColor = ColorBrandPrimary,
                focusedTextColor = ColorGray900,
                unfocusedTextColor = ColorGray900,
            ),
        )

        if (isError && errorText != null) {
            Spacer(Modifier.height(6.dp))
            Text(text = errorText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ColorSystemError)
        }
    }
}
