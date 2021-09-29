package merchant.okcredit.accounting.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

@Keep
data class TransactionImage(
    @SerializedName("id") var id: String?,
    @SerializedName("request_id") var request_id: String,
    @SerializedName("transaction_id") val transaction_id: String?,
    @SerializedName("url") var url: String,
    @SerializedName("create_time") var create_time: DateTime,
    val imageUrl: String? = null,
)
