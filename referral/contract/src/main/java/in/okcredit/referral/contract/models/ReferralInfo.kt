package `in`.okcredit.referral.contract.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ReferralInfo(
    @SerializedName("referral_price")
    val referralPrice: Long?,
    @SerializedName("max_amount")
    val maxAmount: Long?,
    @SerializedName("status")
    val status: Int
)
