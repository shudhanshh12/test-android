package `in`.okcredit.collection_ui.ui.inventory.create_bill.view

import `in`.okcredit.collection.contract.InventoryItem

sealed class InventoryListItem {
    data class InventoryItemViewToShow(
        val inventoryItem: InventoryItem,
    ) : InventoryListItem()
}
