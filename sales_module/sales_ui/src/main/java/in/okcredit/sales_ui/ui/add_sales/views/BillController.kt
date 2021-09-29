package `in`.okcredit.sales_ui.ui.add_sales.views

import `in`.okcredit.sales_ui.ui.add_sales.AddSaleContract
import com.airbnb.epoxy.AsyncEpoxyController

class BillController : AsyncEpoxyController() {

    private var state: AddSaleContract.State? = null

    fun setState(state: AddSaleContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        state?.let {
            if (it.amount >= 0) {
                for (billItem in it.billItems) {
                    billView {
                        id(billItem.name)
                        billItem(billItem)
                    }
                }
            }
        }
    }
}
