package `in`.okcredit.sales_ui.ui.add_bill_dialog

import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface AddBillContract {
    data class State(
        val canShowRateAndQuantity: Boolean = false,
        val enableSave: Boolean = false,
        val name: String = "",
        val rate: Double = 0.0,
        val quantity: Double = 0.0
    ) : UiState

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class ShowErrorIntent(val msg: String) : Intent()

        data class AddBillItemIntent(val addBillItemRequest: BillModel.AddBillItemRequest) : Intent()

        data class UpdateBillItemIntent(val billId: String, val updateBillItemIntent: BillModel.UpdateBillItemRequest) : Intent()

        data class UpdateQuantityIntent(val billItem: BillModel.BillItem) : Intent()

        data class SetNameIntent(val name: String) : Intent()

        data class SetRateIntent(val rate: String) : Intent()

        data class SetQuantityIntent(val quantity: String) : Intent()

        data class SetDataIntent(val billItem: BillModel.BillItem?) : Intent()

        object ShowRateAndQuantityIntent : Intent()

        data class SetSaveEnableIntent(val isEnable: Boolean) : Intent()

        data class PlusIntent(val qty: String) : Intent()

        data class MinusIntent(val qty: String) : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetName(val name: String) : PartialState()

        data class SetRate(val rate: Double) : PartialState()

        data class SetQuantity(val quantity: Double) : PartialState()

        object ShowRateAndQuantity : PartialState()

        data class SetSaveEnable(val isEnable: Boolean) : PartialState()

        data class SetData(val name: String, val rate: Double, val quantity: Double) :
            PartialState()
    }

    interface Navigator {
        fun goToLogin()

        fun showError(msg: String? = null)

        fun onBillItemAdded(billItem: BillModel.BillItem)

        fun onBillItemUpdated(billItem: BillModel.BillItem)

        fun onAddFailed(msg: String)

        fun onUpdateFailed(msg: String)

        fun updateQuantity(qty: String)
    }
}
