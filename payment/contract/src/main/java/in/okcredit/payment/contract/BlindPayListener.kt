package `in`.okcredit.payment.contract

interface BlindPayListener {
    fun onPaymentTypeSelected(paymentType: PaymentType)
}

enum class PaymentType(type: String) {
    BLIND_PAY("blindPay"),
    OTHERS("others"), // like UPI, Bank etc
}
