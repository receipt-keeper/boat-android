package com.windrr.boat.data.remote.model

data class FileUploadResponse(
    val success: Boolean,
    val status: Int,
    val data: FileUploadData,
)

data class FileUploadData(
    val files: List<UploadedFile>,
)

data class UploadedFile(
    val contentPath: String,
    val contentType: String,
    val fileId: String,
    val originalName: String,
    val size: Long,
)
