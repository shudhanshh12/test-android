package `in`.okcredit.collection_ui.ui.inventory.create_bill

import `in`.okcredit.collection_ui.ui.inventory.create_bill.view.InventoryListItem
import `in`.okcredit.collection_ui.ui.inventory.create_bill.view.inventoryItemView
import com.airbnb.epoxy.TypedEpoxyController
import javax.inject.Inject

class InventoryItemListController @Inject constructor(private val fragment: InventoryItemListFragment) :
    TypedEpoxyController<List<InventoryListItem>>() {
    override fun buildModels(data: List<InventoryListItem>?) {
        data?.forEach { data ->
            when (data) {
                is InventoryListItem.InventoryItemViewToShow -> {
                    inventoryItemView {
                        id(data.inventoryItem.item)
                        billItem(data.inventoryItem)
                        listener(fragment)
                    }
                }
            }
        }
    }
}
