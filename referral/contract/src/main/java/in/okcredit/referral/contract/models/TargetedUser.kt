package `in`.okcredit.referral.contract.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class TargetedUser(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("mobile")
    val phoneNumber: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("converted")
    val converted: Boolean = false,
    @SerializedName("amount")
    val amount: Long? = null
)
