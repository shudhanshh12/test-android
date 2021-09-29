package `in`.okcredit.merchant.customer_ui.data.server.model.response

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class Subscription(
    @SerializedName("id")
    val id: String,
    @SerializedName("account_id")
    val accountId: String,
    @SerializedName("amount")
    val amount: Long,
    @SerializedName("end_date")
    val endDate: Long?,
    @SerializedName("frequency")
    val frequency: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("next_schedule")
    val nextSchedule: Long?,
    @SerializedName("start_date")
    val startDate: Long,
    @SerializedName("status")
    val status: Int,
    @SerializedName("update_time")
    val updateTime: Long?,
    @SerializedName("create_time")
    val createTime: Long,
    @SerializedName("week")
    val week: List<Int>?
) : Parcelable
