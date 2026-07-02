package com.windrr.boat.feature.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.windrr.boat.core.ocr.OcrResult
import com.windrr.boat.core.util.toPriceString
import com.windrr.boat.data.repository.OcrRepositoryImpl
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun OcrTestScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var result by remember { mutableStateOf<OcrResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val scope = rememberCoroutineScope()
    val ocrRepo = remember { OcrRepositoryImpl() }

    fun processUri(uri: Uri) {
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

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { processUri(it) }
    }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { processUri(it) }
    }

    fun launchCamera() {
        val dir = File(context.cacheDir, "images").also { it.mkdirs() }
        val file = File(dir, "receipt_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUri = uri
        camera.launch(uri)
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchCamera() }

    Column(modifier = Modifier.fillMaxSize()) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("갤러리")
                }
                OutlinedButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            launchCamera()
                        } else {
                            cameraPermission.launch(Manifest.permission.CAMERA)
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("카메라 촬영")
                }
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

                if (ocr.items.isNotEmpty()) {
                    Text(
                        "구매 상품",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ocr.items.forEachIndexed { idx, item ->
                        Text(
                            text = "${idx + 1}. $item",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                }
                OcrResultRow("구매일", ocr.purchaseDateIso)
                OcrResultRow("결제 금액", ocr.price?.let { "${it.toPriceString()}원" })
                OcrResultRow("제품명", ocr.productName)
                OcrResultRow("브랜드", ocr.brand)
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
