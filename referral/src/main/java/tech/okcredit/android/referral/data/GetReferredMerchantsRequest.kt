package tech.okcredit.android.referral.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GetReferredMerchantsRequest(
    @SerializedName("merchant_id")
    val merchantId: String
)
