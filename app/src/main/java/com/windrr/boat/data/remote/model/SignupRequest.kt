package com.windrr.boat.data.remote.model

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("idToken")          val idToken: String,
    @SerializedName("termsAccepted")    val termsAccepted: Boolean,
    @SerializedName("privacyAccepted")  val privacyAccepted: Boolean,
    @SerializedName("marketingConsent") val marketingConsent: Boolean,
    @SerializedName("termsVersion")     val termsVersion: String = "2026-06-01",
    @SerializedName("privacyVersion")   val privacyVersion: String = "2026-06-01",
)
