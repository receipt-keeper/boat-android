package com.windrr.boat.data.remote.model

data class UsageResponse(
    val success: Boolean,
    val status: Int,
    val data: UsageData,
)

data class UsageData(
    val ocr: OcrUsage,
)

data class OcrUsage(
    val canAnalyze: Boolean = false,
    val remainingCount: Int = 0,
)
