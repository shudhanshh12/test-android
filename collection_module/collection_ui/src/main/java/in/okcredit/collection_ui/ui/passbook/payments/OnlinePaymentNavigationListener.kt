package `in`.okcredit.collection_ui.ui.passbook.payments

interface OnlinePaymentNavigationListener {
    fun moveToPaymentDetail(paymentId: String, customerId: String? = null)
}
