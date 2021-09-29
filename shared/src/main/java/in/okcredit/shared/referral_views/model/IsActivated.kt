package `in`.okcredit.shared.referral_views.model

import com.google.gson.annotations.SerializedName

data class IsActivated(
    @SerializedName("name")
    val name: String,
    @SerializedName("count")
    val count: Long
)
