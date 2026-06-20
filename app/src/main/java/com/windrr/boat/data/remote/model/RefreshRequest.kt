package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * POST /api/v1/auth/refresh 요청 본문
 *
 * 직전 로그인 또는 재발급에서 받은 refreshToken 원문을 담는다.
 * 서버는 이 토큰을 1회용으로 회전(rotate)해 새 토큰 쌍을 발급한다.
 */
data class RefreshRequest(
    @SerializedName("refreshToken") val refreshToken: String,
)
