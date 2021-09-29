package `in`.okcredit.collection_ui.ui.inventory.items

import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection_ui.ui.inventory.view.InventoryTabListItem
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface InventoryItemContract {

    data class State(
        val inventoryList: List<InventoryItem> = listOf(),
        val inventoryTabListItem: List<InventoryTabListItem> = listOf(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetItemList(val itemList: List<InventoryItem>) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class CreateItem(var inventoryItem: InventoryItem) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {

        data class ShowError(val err: Int) : ViewEvents()
        data class FillProductDetails(var productName: String) : ViewEvents()
    }
}
