package `in`.okcredit.merchant.customer_ui.ui.customerreports

import `in`.okcredit.merchant.customer_ui.BuildConfig
import `in`.okcredit.merchant.customer_ui.ui.customerreports.views.customerReportsView
import `in`.okcredit.merchant.customer_ui.ui.customerreports.views.noRecordsView
import com.airbnb.epoxy.AsyncEpoxyController

class CustomerReportsController : AsyncEpoxyController() {
    private lateinit var state: CustomerReportsContract.State

    init {
        isDebugLoggingEnabled = BuildConfig.DEBUG
    }

    fun setState(state: CustomerReportsContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {

        state.transactions?.let { transactions ->
            if (transactions.isEmpty()) {
                noRecordsView {
                    id("noRecordsView")
                }
            } else {
                transactions.forEach {
                    customerReportsView {
                        id("customerreportsView_${it.id}")
                        transaction(it)
                    }
                }
            }
        }
    }
}
