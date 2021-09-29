package `in`.okcredit.referral.contract.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ShareContent(
    @SerializedName("content")
    val text: String,
    @SerializedName("image_icon")
    val imageUrl: String
)
