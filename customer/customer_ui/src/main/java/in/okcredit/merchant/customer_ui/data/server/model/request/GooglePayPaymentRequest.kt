package `in`.okcredit.merchant.customer_ui.data.server.model.request

import com.google.gson.annotations.SerializedName

data class GooglePayPaymentRequest(
    @SerializedName("amount")
    val amount: Long,
    @SerializedName("customer_mobile_number")
    val customer_mobile_number: String,
    @SerializedName("merchant_name")
    val merchant_name: String,
    @SerializedName("transaction_id")
    val transactionId: String,
    @SerializedName("link_id")
    val linkId: String,
    @SerializedName("customer_id")
    val customerId: String,
)
