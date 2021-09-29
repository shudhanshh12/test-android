package `in`.okcredit.collection_ui.ui.inventory.items

import `in`.okcredit.collection.contract.InventoryEpoxyModel
import `in`.okcredit.collection_ui.ui.inventory.view.InventoryTabListItem
import `in`.okcredit.collection_ui.ui.inventory.view.inventoryTabItemView
import com.airbnb.epoxy.TypedEpoxyController
import javax.inject.Inject

class InventoryItemController @Inject constructor(private val fragment: InventoryItemFragment) :
    TypedEpoxyController<List<InventoryTabListItem>>() {

    override fun buildModels(data: List<InventoryTabListItem>?) {
        data?.forEach { data ->
            when (data) {
                is InventoryTabListItem.InventoryTabItemToShow -> {
                    inventoryTabItemView {
                        id(data.inventoryEpoxyModel.inventoryItem.item)
                        billingData(
                            InventoryEpoxyModel(
                                inventoryItem = data.inventoryEpoxyModel.inventoryItem,
                                source = data.inventoryEpoxyModel.source
                            )
                        )
                        listener(fragment)
                    }
                }
            }
        }
    }
}
