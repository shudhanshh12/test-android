package `in`.okcredit.onboarding.marketing

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MarketingData(
    @SerializedName("aa_id")
    @Json(name = "aa_id")
    val aaId: String,

    @SerializedName("is_sign_up")
    @Json(name = "is_sign_up")
    val isSignUp: Boolean,

    @SerializedName("login_time")
    @Json(name = "login_time")
    val loginTime: Long,

    @SerializedName("appsflyer")
    @Json(name = "appsflyer")
    val appsflyer: Appsflyer,
)

@JsonClass(generateAdapter = true)
data class Appsflyer(

    @SerializedName("media_source")
    @Json(name = "media_source")
    val mediaSource: String,

    @SerializedName("campaign")
    @Json(name = "campaign")
    val campaign: String,
)
