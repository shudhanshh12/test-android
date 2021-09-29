package tech.okcredit.android.referral.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class JourneyQualificationRequest(
    @SerializedName("merchant_id")
    val merchantId: String
)
