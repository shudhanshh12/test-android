package `in`.okcredit.onboarding.businessname.models

import com.google.gson.annotations.SerializedName

data class BusinessStats(
    @SerializedName("count")
    val count: String,

    @SerializedName("duration_type")
    val durationType: String
)
