package `in`.okcredit.collection_ui.ui.inventory.bills

import `in`.okcredit.collection.contract.InventoryBillItemResponse
import `in`.okcredit.collection_ui.ui.inventory.view.InventoryTabListItem
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface InventoryBillContract {

    data class State(
        val inventoryBillList: List<InventoryBillItemResponse> = listOf(),
        val inventoryTabListItem: List<InventoryTabListItem> = listOf(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetItemList(val itemListInventory: List<InventoryBillItemResponse>) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class OpenBillWebView(val billId: String, val merchantId: String) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        data class ShowError(val err: Int) : ViewEvents()
        data class OpenBillWebView(val billId: String, val merchantId: String) : ViewEvents()
    }
}
