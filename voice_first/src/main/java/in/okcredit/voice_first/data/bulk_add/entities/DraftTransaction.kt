package `in`.okcredit.voice_first.data.bulk_add.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

const val MERCHANT_TYPE_CUSTOMER = "customer"
const val MERCHANT_TYPE_SUPPLIER = "supplier"

const val TRANSACTION_TYPE_CREDIT = "credit"
const val TRANSACTION_TYPE_PAYMENT = "payment"

@Keep
data class DraftTransaction(
    @SerializedName("draft_transaction_id")
    val draftTransactionId: String, // track draft transactions throughout it life
    @SerializedName("voice_transcript")
    val voiceTranscript: String,
    @SerializedName("is_parsed")
    val isParsed: Boolean = false,

    @SerializedName("transaction_type")
    val transactionType: String? = null, // "credit" / "payment"
    @SerializedName("amount")
    val amount: Long? = null,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("draft_merchants")
    val draftMerchants: List<DraftMerchant>? = null,
)

@Keep
data class DraftMerchant(
    @SerializedName("merchant_id")
    val merchantId: String,
    @SerializedName("merchant_type")
    val merchantType: String, // "customer" / "supplier"
    @SerializedName("merchant_name")
    val merchantName: String? = null, // Name of the Merchant
)

fun DraftTransaction.isComplete() =
    isParsed &&
        transactionType != null &&
        amount != null && amount > 0 &&
        draftMerchants != null && draftMerchants.isNotEmpty()
