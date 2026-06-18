package com.windrr.boat.data.repository

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import com.windrr.boat.AppCore
import com.windrr.boat.BuildConfig
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.core.ocr.OcrResult
import com.windrr.boat.core.ocr.ReceiptTextParser
import com.windrr.boat.data.remote.VisionApiClient
import com.windrr.boat.data.remote.model.AnnotateImageRequest
import com.windrr.boat.data.remote.model.VisionAnnotateRequest
import com.windrr.boat.data.remote.model.VisionFeature
import com.windrr.boat.data.remote.model.VisionImage
import com.windrr.boat.data.remote.model.VisionImageContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

class OcrRepositoryImpl : OcrRepository {

    private val context get() = AppCore.instance

    override suspend fun extractFromImage(imageUri: Uri): Result<OcrResult> = runCatching {
        withContext(Dispatchers.IO) {
            val base64Image = encodeImageToBase64(imageUri)

            val request = VisionAnnotateRequest(
                requests = listOf(
                    AnnotateImageRequest(
                        image = VisionImage(content = base64Image),
                        features = listOf(VisionFeature(type = "DOCUMENT_TEXT_DETECTION")),
                        imageContext = VisionImageContext(languageHints = listOf("ko"))
                    )
                )
            )

            val response = VisionApiClient.service.annotate(
                apiKey = BuildConfig.VISION_API_KEY,
                request = request
            )

            val apiError = response.responses.firstOrNull()?.error
            if (apiError != null) {
                throw IOException("Vision API 오류 ${apiError.code}: ${apiError.message}")
            }

            val fullText = response.responses.firstOrNull()?.fullTextAnnotation?.text
            if (fullText.isNullOrBlank()) {
                BoatLog.w("Vision API: 텍스트를 인식하지 못했습니다")
                return@withContext OcrResult()
            }

            BoatLog.d("Vision API 텍스트 추출 완료 (${fullText.length}자)")
            ReceiptTextParser.parse(fullText)
        }
    }

    // Uri → Bitmap → 리사이즈 → JPEG Base64
    private fun encodeImageToBase64(uri: Uri): String {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, uri)
            ) { decoder, _, _ -> decoder.isMutableRequired = false }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val resized = bitmap.resizeToMax(MAX_IMAGE_PX)
        val output = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    private fun Bitmap.resizeToMax(maxPx: Int): Bitmap {
        if (width <= maxPx && height <= maxPx) return this
        val ratio = minOf(maxPx.toFloat() / width, maxPx.toFloat() / height)
        return Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
    }

    companion object {
        private const val MAX_IMAGE_PX = 1600   // 긴 변 기준 — API 비용·속도 최적화
        private const val JPEG_QUALITY = 85
    }
}
