package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes

interface AddTxnContainerContract {

    data class State(
        val loading: Boolean = true,
        val customerId: String = "",
        val customerName: String = "",
        val customerProfile: String? = null,
        val balanceDue: Long = 0L,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SetLoading(val loading: Boolean) : PartialState()

        data class CustomerData(val customer: Customer) : PartialState()

        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class ShowError(@StringRes val error: Int) : ViewEvent()
    }
}
