package tech.okcredit.android.referral.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ReferredMerchant(
    val name: String?,
    @SerializedName("phone_number")
    val phoneNumber: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    val rewards: List<ReferralReward>?,
    @SerializedName("can_notify")
    val canNotify: Boolean = false,
    @SerializedName("pending_amount")
    val pendingAmount: String? = "",
    @SerializedName("time_diff")
    val enableTime: String? = ""
)
