package tech.okcredit.android.referral.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GetReferredMerchantsResponse(
    @SerializedName("referred_merchants")
    val referredMerchants: List<ReferredMerchant>
)
