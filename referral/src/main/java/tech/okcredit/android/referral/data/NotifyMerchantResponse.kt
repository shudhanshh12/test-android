package tech.okcredit.android.referral.data

import androidx.annotation.Keep

@Keep
data class NotifyMerchantResponse(
    val code: Int?,
    val error: String?
)
