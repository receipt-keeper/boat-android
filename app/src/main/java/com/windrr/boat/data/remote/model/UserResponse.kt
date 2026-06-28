package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName
import com.windrr.boat.data.model.User

/**
 * GET /api/v1/users/me 응답 (envelope: success/status/data)
 */
data class UserResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("status")  val status: Int,
    @SerializedName("data")    val data: UserData,
)

data class UserData(
    @SerializedName("email")                       val email: String?,
    @SerializedName("name")                        val name: String?,
    @SerializedName("nickname")                    val nickname: String?,
    @SerializedName("profileImageUrl")             val profileImageUrl: String?,
    @SerializedName("freeAnalysisTokensRemaining") val freeAnalysisTokensRemaining: Int = 0,
)

/** 서버 DTO → 앱 도메인 모델 매핑 (알림 설정은 별도 API에서 채워진다) */
fun UserData.toUser(): User = User(
    email = email.orEmpty(),
    name = name.orEmpty(),
    nickname = nickname.orEmpty(),
    profileImageUrl = profileImageUrl.orEmpty(),
    freeAnalysisTokensRemaining = freeAnalysisTokensRemaining,
)
