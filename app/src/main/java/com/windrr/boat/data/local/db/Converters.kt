package com.windrr.boat.data.local.db

import androidx.room.TypeConverter

/** Room 타입 컨버터 — List<String>을 구분자 문자열로 직렬화 (fileId 목록 저장용) */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String = value?.joinToString(SEPARATOR).orEmpty()

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList() else value.split(SEPARATOR)

    private companion object {
        // 파일 ID는 UUID라 이 구분자와 충돌하지 않음
        const val SEPARATOR = "|"
    }
}
