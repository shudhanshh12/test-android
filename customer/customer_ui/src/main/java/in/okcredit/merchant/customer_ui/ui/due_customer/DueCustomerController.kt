package `in`.okcredit.merchant.customer_ui.ui.due_customer

import `in`.okcredit.merchant.customer_ui.ui.due_customer.views.*
import com.airbnb.epoxy.AsyncEpoxyController
import javax.inject.Inject

class DueCustomerController @Inject constructor(private val dueCustomerFragment: DueCustomerFragment) :
    AsyncEpoxyController() {

    private lateinit var state: DueCustomerContract.State

    fun setState(state: DueCustomerContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {

        if (state.dueCustomers.isNotEmpty()) {
            state.dueCustomers.map {
                dueCustomerItemView {
                    id(it.id)
                    customer(it)
                    selectCustomers(state.selectedCustomerIds.contains(it.id))
                    clickListener(dueCustomerFragment)
                }
            }
        } else if (!state.searchQuery.isNullOrBlank()) {
            emptyContactPlaceholderView {
                id("emptyContactPlaceholderView")
                searchName(state.searchQuery)
            }
        }
    }
}
