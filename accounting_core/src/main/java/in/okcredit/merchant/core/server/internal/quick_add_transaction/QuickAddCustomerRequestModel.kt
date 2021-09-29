package `in`.okcredit.merchant.core.server.internal.quick_add_transaction

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class QuickAddCustomerRequestModel(
    @SerializedName("description")
    @Json(name = "description")
    val name: String,
    val mobile: String? = null,
    @SerializedName("profile_image")
    @Json(name = "profile_image")
    val profileImage: String? = null,
    val reactivate: Boolean = false
)
