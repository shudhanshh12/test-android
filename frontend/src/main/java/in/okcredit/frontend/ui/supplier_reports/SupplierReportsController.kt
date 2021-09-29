package `in`.okcredit.frontend.ui.supplier_reports

import `in`.okcredit.frontend.ui.supplier_reports.views.supplierReportsView
import `in`.okcredit.merchant.customer_ui.BuildConfig
import `in`.okcredit.merchant.customer_ui.ui.customerreports.views.noRecordsView
import com.airbnb.epoxy.AsyncEpoxyController

class SupplierReportsController : AsyncEpoxyController() {
    private lateinit var state: SupplierReportsContract.State

    init {
        isDebugLoggingEnabled = BuildConfig.DEBUG
    }

    fun setState(state: SupplierReportsContract.State) {
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
                transactions.map {
                    supplierReportsView {
                        id("customerreportsView_${it.id}")
                        transaction(it)
                    }
                }
            }
        }
    }
}
