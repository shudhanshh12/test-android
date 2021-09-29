package tech.okcredit.android.referral.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NotifyMerchantRequest(
    @SerializedName("merchant_id")
    val merchantId: String,
    @SerializedName("referred_phone_number")
    val phoneNumber: String
)
