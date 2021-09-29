package `in`.okcredit.frontend.contract

interface AccountingEventTracker {

    fun trackAddTransactionConfirm(type: String, amount: Long, flow: String, commonLedger: Boolean)
}
