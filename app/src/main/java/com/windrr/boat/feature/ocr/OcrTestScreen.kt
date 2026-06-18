package com.windrr.boat.feature.ocr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.core.ocr.OcrResult
import com.windrr.boat.data.repository.OcrRepositoryImpl
import kotlinx.coroutines.launch

@Composable
fun OcrTestScreen(onBack: () -> Unit) {
    var result by remember { mutableStateOf<OcrResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val ocrRepo = remember { OcrRepositoryImpl() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        isLoading = true
        error = null
        result = null
        scope.launch {
            ocrRepo.extractFromImage(uri)
                .onSuccess { result = it }
                .onFailure { error = it.message }
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← 뒤로") }
            Text(
                text = "OCR 테스트",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Button(
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (isLoading) "인식 중..." else "영수증 사진 선택")
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Vision API 분석 중...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            error?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Text("오류: $it", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            result?.let { ocr ->
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("인식 결과", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OcrResultRow("제품명", ocr.productName)
                OcrResultRow("브랜드", ocr.brand)
                OcrResultRow("가격", ocr.price?.let { "${String.format("%,d", it)}원" })
                OcrResultRow("구매일", ocr.purchaseDateIso)
                OcrResultRow("보증기간", ocr.warrantyMonths?.let { "${it}개월" })
                OcrResultRow("시리얼 넘버", ocr.serialNumber)
                OcrResultRow("대분류", ocr.category.displayName, alwaysShow = true)

                if (ocr.isEmpty) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "텍스트를 인식했지만 필드를 추출하지 못했습니다",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OcrResultRow(label: String, value: String?, alwaysShow: Boolean = false) {
    val displayValue = value ?: if (alwaysShow) value else null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(
            text = value ?: if (alwaysShow) "-" else "인식 실패",
            fontWeight = if (value != null) FontWeight.Medium else FontWeight.Normal,
            color = if (value != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}
