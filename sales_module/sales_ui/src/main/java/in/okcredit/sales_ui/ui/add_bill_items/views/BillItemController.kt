package `in`.okcredit.sales_ui.ui.add_bill_items.views

import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.ui.add_bill_items.AddBillItemsContract
import `in`.okcredit.sales_ui.ui.add_bill_items.AddBillItemsFragment
import com.airbnb.epoxy.AsyncEpoxyController

class BillItemController constructor(val fragment: AddBillItemsFragment) : AsyncEpoxyController() {

    private var state: AddBillItemsContract.State? = null

    fun setState(state: AddBillItemsContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        state?.let {

            // new items added should be displayed at top of the list
            // rest of the list should be sorted in ascending order

            val nonBilledItems =
                it.inventoryItems?.filter { billItem -> it.billedItems?.items?.find { it.id == billItem.id } == null && it.newItems.find { it.id == billItem.id } == null } ?: listOf()

            val nonNewAddedItems =
                it.addedItems.filter { billItem -> it.newItems.find { it.id == billItem.id } == null }

            var list = it.newItems.reversed() + (nonNewAddedItems + nonBilledItems).sortedBy { item -> item.name.toLowerCase() }
            list = list.filter { item ->
                item.name.toLowerCase().startsWith(it.searchQuery.toLowerCase())
            }

            if (it.searchQuery.isNotEmpty() && list.isEmpty()) {
                addBillView {
                    id(it.searchQuery)
                    billItem(AddBillView.Model(BillModel.BillItem("", it.searchQuery, quantity = 0.0), true))
                    listener(fragment)
                }
            }

            for (item in list) {
                addBillView {
                    id(item.name)
                    billItem(AddBillView.Model(item))
                    listener(fragment)
                }
            }
        }
    }
}
