package tech.okcredit.user_migration.contract.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Data(
    @SerializedName("customer_object_id")
    val customer_object_id: String?,
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("mobile")
    val mobile: String? = "",
    @SerializedName("transaction")
    val transaction: Transaction?,
    @SerializedName("error")
    val error: List<String>
) : Serializable

@Keep
data class GetPredictedDataApiRequest(
    @SerializedName("url")
    val imageUrl: String,
    @SerializedName("merchant_id")
    val merchantId: String
)

@Keep
data class PredictedData(
    @SerializedName("transaction")
    val amountBox: AmountBox,
    @SerializedName("width")
    val width: Int = 0,
    @SerializedName("height")
    val height: Int = 0,
    @SerializedName("file")
    val fileName: String = "",
    @SerializedName("file_object_id")
    val fileObjectId: String = ""
)

@Keep
data class AmountBox(
    val amount: Long = 0L,
    @SerializedName("box_x1")
    val boxCoordinateX1: Int? = null,
    @SerializedName("box_x2")
    val boxCoordinateX2: Int? = null,
    @SerializedName("box_y1")
    val boxCoordinateY1: Int? = null,
    @SerializedName("box_y2")
    val boxCoordinateY2: Int? = null,
)

data class SetAmountAmendedApiRequest(
    @SerializedName("merchant_id")
    val merchantId: String,
    @SerializedName("customer_account_id")
    val customerAccountId: String,
    @SerializedName("file_upload_id")
    val fileUploadId: String = "",
    @SerializedName("transaction_id")
    val transactionId: String,
    @SerializedName("amount")
    val newAmount: Long
)
