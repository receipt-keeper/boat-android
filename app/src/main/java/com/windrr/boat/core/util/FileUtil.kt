package com.windrr.boat.core.util

import java.io.File

/**
 * 파일 관련 유틸리티 함수 모음
 */

private const val BYTES_PER_MB = 1024L * 1024L

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
