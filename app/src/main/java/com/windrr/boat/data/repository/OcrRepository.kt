package com.windrr.boat.data.repository

import android.net.Uri
import com.windrr.boat.core.ocr.OcrResult

interface OcrRepository {
    /**
     * 영수증 이미지 URI를 Cloud Vision API로 전송해 OCR 결과를 반환한다.
     *
     * @return [Result.success] — 파싱 성공 (일부 필드는 null일 수 있음)
     *         [Result.failure] — 네트워크 오류 또는 API 에러
     */
    suspend fun extractFromImage(imageUri: Uri): Result<OcrResult>
}
