package com.windrr.boat.core.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * 파일 관련 유틸리티 함수 모음
 */

private const val BYTES_PER_MB = 1024L * 1024L

/** FileProvider authority — AndroidManifest의 provider 선언과 일치해야 함 */
private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

/**
 * 파일 크기가 지정한 MB 제한을 초과하는지 확인
 *
 * @param limitMb 크기 제한 (기본값: 10MB)
 * @return 제한 초과 시 true
 */
fun File.isOverSizeLimit(limitMb: Int = 10): Boolean {
    return length() > limitMb * BYTES_PER_MB
}

/**
 * 파일 크기를 MB 단위로 반환
 */
fun File.sizeInMb(): Double {
    return length().toDouble() / BYTES_PER_MB
}

/**
 * 카메라 촬영 결과를 저장할 임시 이미지 파일의 content URI 생성.
 *
 * 캐시 디렉터리(cacheDir/images) 아래에 임시 파일을 만들고,
 * FileProvider를 통해 외부 카메라 앱이 쓸 수 있는 content:// URI로 변환한다.
 *
 * @return 촬영 결과가 저장될 content URI
 */
fun Context.createImageCaptureUri(): Uri {
    val imagesDir = File(cacheDir, "images").apply { mkdirs() }
    val imageFile = File.createTempFile("receipt_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        this,
        "$packageName$FILE_PROVIDER_SUFFIX",
        imageFile
    )
}

/**
 * content URI를 멀티파트 업로드용 [MultipartBody.Part]로 변환.
 * MIME 타입과 표시 이름(DISPLAY_NAME)을 조회해 채운다.
 *
 * 디스크/ContentProvider I/O(readBytes)가 포함되어 있어 Dispatchers.IO에서 실행한다.
 * 호출부가 rememberCoroutineScope() 등 메인 스레드 기반 스코프여도 이 함수 자체가
 * 안전하게 IO로 전환하므로 UI 프리징 없이, 또 토큰 만료 전에 더 빨리 업로드를 시작할 수 있다.
 *
 * @param fieldName 서버가 기대하는 form-data field 이름
 */
suspend fun Uri.toMultipartPart(context: Context, fieldName: String): MultipartBody.Part =
    withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(this@toMultipartPart) ?: "image/jpeg"
        var fileName = "file.jpg"
        resolver.query(this@toMultipartPart, null, null, null, null)?.use { cursor ->
            val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (col != -1 && cursor.moveToFirst()) fileName = cursor.getString(col)
        }
        val bytes = resolver.openInputStream(this@toMultipartPart)!!.use { it.readBytes() }
        val body = bytes.toRequestBody(mimeType.toMediaType())
        MultipartBody.Part.createFormData(fieldName, fileName, body)
    }
