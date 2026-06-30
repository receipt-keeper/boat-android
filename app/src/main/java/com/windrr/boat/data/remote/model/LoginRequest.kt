package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("idToken") val idToken: String,
)
