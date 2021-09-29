package `in`.okcredit.merchant.customer_ui.data.server.model.response

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ActiveStaffLinkResponse(
    @SerializedName("account_ids")
    val accountIds: List<String>?,
    @SerializedName("link")
    val link: String?,
    @SerializedName("link_id")
    val linkId: String?,
    @SerializedName("create_time")
    val createTime: Long?,
) : Parcelable
