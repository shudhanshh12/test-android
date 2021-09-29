package `in`.okcredit.payment.ui.payment_error_screen

enum class PaymentErrorType(val value: String) {
    NETWORK("network"),
    OTHER("other");

    companion object {
        val map = values().associateBy(PaymentErrorType::value)
        fun fromValue(value: String) = map[value] ?: OTHER
    }
}
