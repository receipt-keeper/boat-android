package com.windrr.boat.core.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
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
