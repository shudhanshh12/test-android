package tech.okcredit.android.referral.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ReferralReward(
    val id: String?,
    @SerializedName("referrer_merchant_prize")
    val referrerPrize: String?,
    @SerializedName("referred_merchant_prize")
    val referredPrize: String?,
    val events: List<Event>?,
    @SerializedName("title")
    val nextRewardTitle: String?,
    @SerializedName("description")
    val nextRewardDescription: String?,
    @SerializedName("referrer_title")
    val referrerTitle: String = "",
    @SerializedName("referrer_description")
    val referrerDescription: String = ""
)
