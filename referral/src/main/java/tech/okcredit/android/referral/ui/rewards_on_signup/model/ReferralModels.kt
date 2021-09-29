package tech.okcredit.android.referral.ui.rewards_on_signup.model

import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import com.google.gson.annotations.SerializedName

data class GetReferralTargetsApiRequest(
    @SerializedName("merchant_id")
    val merchantId: String
)

data class GetReferralTargetsApiResponse(
    @SerializedName("rewards")
    val rewards: List<ReferralTargetBanner>
)
