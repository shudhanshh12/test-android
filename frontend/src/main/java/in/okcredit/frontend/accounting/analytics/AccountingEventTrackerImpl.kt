package `in`.okcredit.frontend.accounting.analytics

import `in`.okcredit.analytics.AnalyticsEvents.ADD_TXN_CONFIRM
import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyKey.COMMON_LEDGER
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.frontend.contract.AccountingEventTracker
import dagger.Lazy
import javax.inject.Inject

class AccountingEventTrackerImpl @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) :
    AccountingEventTracker {

    override fun trackAddTransactionConfirm(type: String, amount: Long, flow: String, commonLedger: Boolean) {
        val properties = mapOf(
            PropertyKey.TYPE to type,
            PropertyKey.AMOUNT.toLowerCase() to amount,
            PropertyKey.RELATION to PropertyValue.CUSTOMER,
            PropertyKey.FLOW to flow,
            COMMON_LEDGER to commonLedger,
        )
        analyticsProvider.get().trackEvents(ADD_TXN_CONFIRM, properties)
    }
}
