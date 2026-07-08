package com.windrr.boat.data.remote.interceptor

import com.windrr.boat.core.log.BoatLog
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 첨부 파일 실 조회(GET .../files/{id}/content) 응답이 실제로 어떤 형태로 내려오는지
 * 확인하기 위한 임시 디버그 로그. Content-Type/길이/본문 앞부분을 Logcat(BOAT_FILE)에 남긴다.
 * 원인 파악되면 제거할 것.
 */
class FileContentDebugInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isFileContentRequest = request.url.encodedPath.contains("/files/")
        if (!isFileContentRequest) return chain.proceed(request)

        val authHeader = request.header("Authorization")
        val maskedAuth = authHeader?.let { "있음(len=${it.length}, 끝=...${it.takeLast(8)})" } ?: "없음!!"
        BoatLog.d("[FILE] 요청 → ${request.url} | Authorization=$maskedAuth", tag = TAG)

        val response = chain.proceed(request)

        val contentType = response.header("Content-Type")
        val contentLength = response.header("Content-Length")
        BoatLog.d(
            "[FILE] 응답 ← code=${response.code} contentType=$contentType contentLength=$contentLength",
            tag = TAG,
        )

        // peekBody: 실제 응답 스트림을 소비하지 않고 미리보기만 한다 (Coil 등 실제 소비자에 영향 없음)
        runCatching {
            val looksTextLike = contentType == null ||
                contentType.startsWith("text/") ||
                contentType.contains("json")

            if (looksTextLike) {
                val preview = response.peekBody(PEEK_BYTES).string()
                BoatLog.d("[FILE] 본문 미리보기(텍스트로 추정) = $preview", tag = TAG)
            } else {
                val bytes = response.peekBody(16).bytes()
                val hex = bytes.joinToString(" ") { "%02X".format(it) }
                BoatLog.d("[FILE] 본문 앞 16바이트(hex) = $hex", tag = TAG)
            }
        }.onFailure { e ->
            BoatLog.w("[FILE] 본문 미리보기 실패", throwable = e, tag = TAG)
        }

        return response
    }

    private companion object {
        const val TAG = "BOAT_FILE"
        const val PEEK_BYTES = 2000L
    }
}
