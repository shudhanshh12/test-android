package `in`.okcredit.payment.contract

interface PaymentResultListener {
    fun onRetryClicked()
    fun onNetworkError()
    fun onOtherError()
}
