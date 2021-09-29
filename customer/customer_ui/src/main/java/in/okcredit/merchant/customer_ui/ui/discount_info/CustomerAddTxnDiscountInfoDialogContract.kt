package `in`.okcredit.merchant.customer_ui.ui.discount_info

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface CustomerAddTxnDiscountInfoDialogContract {
    data class State(
        val discountAmount: String = "",
        val creditAmount: String = "",
        val netAmount: String = ""
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SetData(val creditAmount: String, val discountAmount: String, val netAmount: String) :
            PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class ViewEvents : BaseViewEvent
}
