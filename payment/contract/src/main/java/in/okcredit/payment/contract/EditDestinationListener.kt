package `in`.okcredit.payment.contract

interface EditDestinationListener {
    fun onEditDestinationClicked(supplierId: String)
    fun onExitFromPaymentFlow(source: String)
}
