package tech.okcredit.android.referral.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GetReferralLinkResponse(
    @SerializedName("referral_link")
    val referralLink: String
)
