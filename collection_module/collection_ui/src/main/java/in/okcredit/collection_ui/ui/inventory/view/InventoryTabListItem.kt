package `in`.okcredit.collection_ui.ui.inventory.view

import `in`.okcredit.collection.contract.InventoryEpoxyModel

sealed class InventoryTabListItem {
    data class InventoryTabItemToShow(
        val inventoryEpoxyModel: InventoryEpoxyModel,
    ) : InventoryTabListItem()
}
