package `in`.okcredit.merchant.core.server.internal.quick_add_transaction

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tech.okcredit.android.base.utils.DateTimeUtils
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class QuickAddTransactionModel(
    @SerializedName("amount_v2")
    @Json(name = "amount_v2")
    val amount: Long,
    val type: Int, // Transaction.Type.code
    @SerializedName("creator_role")
    @Json(name = "creator_role")
    val creatorRole: String = CreatorRole.SELLER.name,
    @SerializedName("create_time")
    @Json(name = "create_time")
    val createTime: String = DateTimeUtils.currentDateTime().millis.toString(),
    @SerializedName("request_id")
    @Json(name = "request_id")
    val requestId: String = UUID.randomUUID().toString()
)
