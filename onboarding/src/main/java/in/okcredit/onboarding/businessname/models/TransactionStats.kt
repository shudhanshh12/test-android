package `in`.okcredit.onboarding.businessname.models

import com.google.gson.annotations.SerializedName

data class TransactionStats(
    @SerializedName("amount")
    val amount: String,

    @SerializedName("duration_type")
    val durationType: String
)
