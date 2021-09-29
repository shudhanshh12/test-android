package `in`.okcredit.payment.contract.model

interface PaymentModel {
    data class JuspayPaymentPollingModel(
        val status: String,
        val paymentId: String,
        val paymentInfo: PaymentInfo
    )
}

data class PaymentInfo(
    val id: String?,
    val linkId: String?,
    val createTime: Long = System.currentTimeMillis().div(1000),
    val updateTime: Long?,
    val paymentAmount: String?,
    val payoutAmount: String?,
    val refundAmount: String?
)

enum class JuspayPollingStatus(val value: String) {
    SUCCESS("SUCCESS"),
    PENDING("PENDING"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED")
}
