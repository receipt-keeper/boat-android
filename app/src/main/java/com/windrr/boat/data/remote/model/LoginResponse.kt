package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("status")  val status: Int,
    @SerializedName("data")    val data: LoginTokenData,
)

data class LoginTokenData(
    @SerializedName("accessToken")  val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType")    val tokenType: String,
    @SerializedName("expiresIn")    val expiresIn: Int,
)
