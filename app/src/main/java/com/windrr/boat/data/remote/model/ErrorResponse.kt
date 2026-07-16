package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * 공통 에러 응답 envelope.
 * 사용자에게 노출하는 메시지는 data.message.
 */
data class ErrorResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("status")  val status: Int = 0,
    @SerializedName("data")    val data: ErrorData? = null,
)

data class ErrorData(
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("code")      val code: String? = null,
    @SerializedName("message")   val message: String? = null,
    @SerializedName("path")      val path: String? = null,
    @SerializedName("errors")    val errors: List<FieldError>? = null,
)

data class FieldError(
    @SerializedName("field")     val field: String? = null,
    @SerializedName("fileIndex") val fileIndex: Int? = null,
    @SerializedName("message")   val message: String? = null,
)
