package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("idToken")         val idToken: String,
    @SerializedName("termsVersion")    val termsVersion: String? = null,
    @SerializedName("privacyVersion")  val privacyVersion: String? = null,
    @SerializedName("termsAccepted")   val termsAccepted: Boolean = false,
    @SerializedName("privacyAccepted") val privacyAccepted: Boolean = false,
    @SerializedName("marketingConsent") val marketingConsent: Boolean = false,
)
