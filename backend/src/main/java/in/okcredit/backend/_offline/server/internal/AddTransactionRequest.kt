package `in`.okcredit.backend._offline.server.internal

import com.google.gson.annotations.SerializedName
import merchant.okcredit.accounting.model.TransactionImage
import org.joda.time.DateTime

class AddTransactionRequest(
    @SerializedName("customer_id") private val customerId: String,
    @SerializedName("request_id") private val requestId: String,
    @SerializedName("type") private val type: Int,
    @SerializedName("amount_v2") private val amountV2: Long,
    @SerializedName("images") private val receiptUrl: List<TransactionImage>?,
    @SerializedName("note") private val note: String?,
    @SerializedName("created_at") private val timestamp: DateTime,
    @SerializedName("onboarding") private val isOnboarding: Boolean,
    @SerializedName("bill_date") private val billDate: DateTime,
    @SerializedName("sms_sent") private val smsSent: Boolean,
    @SerializedName("intent") private val inputType: String?,
    @SerializedName("intent_id") private val voiceId: String?
)
