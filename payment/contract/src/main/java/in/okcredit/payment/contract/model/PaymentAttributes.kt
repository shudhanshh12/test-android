package `in`.okcredit.payment.contract.model

data class PaymentAttributes(
    val paymentId: String,
    val pollingType: String?,
    val quickPayEnabled: Boolean = false,
)
