package `in`.okcredit.sales_ui.ui.list_sales.views

import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.sales_ui.ui.list_sales.SalesOnCashContract
import `in`.okcredit.sales_ui.ui.list_sales.SalesOnCashFragment
import com.airbnb.epoxy.AsyncEpoxyController

class SalesController(private val fragment: SalesOnCashFragment) : AsyncEpoxyController() {

    private lateinit var states: SalesOnCashContract.State

    data class SalesViewState(
        val sale: Models.Sale,
        val showTime: Boolean
    )

    fun setStates(states: SalesOnCashContract.State) {
        this.states = states
        requestModelBuild()
    }

    override fun buildModels() {
        states.list?.let { sales ->
            for (sale in sales.filter { item -> item.deletedAt == null }) {
                salesView {
                    id(sale.id)
                    sale(SalesViewState(sale, states.filter!! == SalesOnCashFragment.Filter.TODAY))
                    listener(fragment)
                }
            }
        }
    }
}
