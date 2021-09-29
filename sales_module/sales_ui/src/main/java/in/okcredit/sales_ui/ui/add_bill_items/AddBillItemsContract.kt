package `in`.okcredit.sales_ui.ui.add_bill_items

import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface AddBillItemsContract {
    data class State(
        val canSearch: Boolean = false,
        val searchQuery: String = "",
        var newItems: List<BillModel.BillItem> = listOf(),
        var addedItems: List<BillModel.BillItem> = listOf(),
        var inventoryItems: List<BillModel.BillItem>? = null,
        val billedItems: BillModel.BilledItems? = null,
        var totalQuantity: Double = 0.0,
        var total: Double = 0.0
    ) : UiState

    sealed class Intent : UserIntent {
        object Load : Intent()

        object ShowAddBillDialogIntent : Intent()

        data class ShowUpdateBillDialogIntent(val billItem: BillModel.BillItem) : Intent()

        data class SetBillItemsIntent(val billedItems: List<BillModel.BillItem>) : Intent()

        data class NewBillItemIntent(val billItem: BillModel.BillItem) : Intent()

        data class AddBillItemIntent(val billItem: BillModel.BillItem) : Intent()

        data class RemoveBillItemIntent(val billItem: BillModel.BillItem) : Intent()

        data class UpdateBillItemIntent(val billItem: BillModel.BillItem) : Intent()

        data class SearchBillItemIntent(val searchQuery: String) : Intent()

        data class SetBillTotal(val total: Double) : Intent()

        data class AddSale(val amount: Double, val billedItems: BillModel.BilledItems?) : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetInventoryItems(val inventoryItems: List<BillModel.BillItem>) : PartialState()

        data class SetSearchQuery(val searchQuery: String) : PartialState()

        data class SetBillTotal(val total: Double) : PartialState()

        data class SetDataWithNewItems(val billedItems: BillModel.BilledItems, val addedItems: List<BillModel.BillItem>, val totalQuantity: Double, val newlyAddedItems: List<BillModel.BillItem>) : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent {
        object ShowAddBillDialog : ViewEvent()

        object GoToLoginScreen : ViewEvent()

        data class ShowUpdateBillDialog(val billItem: BillModel.BillItem) : ViewEvent()

        object UpdateList : ViewEvent()

        object ScrollToTop : ViewEvent()

        object ClearSearch : ViewEvent()

        data class ShowError(val msg: String) : ViewEvent()

        data class onAddSaleSuccessfull(val saleId: String) : ViewEvent()
    }
}
