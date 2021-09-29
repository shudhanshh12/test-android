package `in`.okcredit.collection_ui.ui.inventory.create_bill

import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection_ui.ui.inventory.create_bill.view.InventoryListItem
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface InventoryItemListContract {

    data class State(
        val error: String = "",
        val addItemList: List<InventoryItem> = listOf(),
        val defaultList: List<InventoryItem> = listOf(),
        val listToShow: Map<String, InventoryItem> = mapOf(),
        val inventoryItemList: List<InventoryListItem> = listOf(),
        val totalBill: Long = 0L,
        val totalQuantity: Int = 0,
        val totalItem: Int = 0,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetDefaultItemList(val itemList: List<InventoryItem>) : PartialState()
        data class AddNewItem(val item: InventoryItem) : PartialState()
        data class DeleteItem(val item: InventoryItem) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class AddItem(var item: InventoryItem) : Intent()
        data class DeleteItem(val item: InventoryItem) : Intent()
        data class CreateBill(val listBillItem: List<InventoryItem>) : Intent()
        object OpenInventoryBottomSheetDialog : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        data class OpenBillWebView(val url: String, val billId: String, val businessId: String) : ViewEvents()
        data class ShowError(val err: Int) : ViewEvents()
        object OpenInventoryBottomSheetDialog : ViewEvents()
    }
}
