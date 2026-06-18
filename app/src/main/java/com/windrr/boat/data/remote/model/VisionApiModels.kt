package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

// ── Request ───────────────────────────────────────────────────────────────────

data class VisionAnnotateRequest(
    val requests: List<AnnotateImageRequest>
)

data class AnnotateImageRequest(
    val image: VisionImage,
    val features: List<VisionFeature>,
    val imageContext: VisionImageContext? = null
)

data class VisionImage(
    val content: String  // Base64 encoded
)

data class VisionFeature(
    val type: String,       // "DOCUMENT_TEXT_DETECTION"
    val maxResults: Int = 1
)

data class VisionImageContext(
    val languageHints: List<String>  // ["ko"]
)

// ── Response ──────────────────────────────────────────────────────────────────

data class VisionAnnotateResponse(
    val responses: List<AnnotateImageResponse>
)

data class AnnotateImageResponse(
    val fullTextAnnotation: FullTextAnnotation?,
    val error: VisionApiError?
)

data class FullTextAnnotation(
    val text: String
)

data class VisionApiError(
    val code: Int,
    val message: String,
    @SerializedName("status") val status: String?
)
