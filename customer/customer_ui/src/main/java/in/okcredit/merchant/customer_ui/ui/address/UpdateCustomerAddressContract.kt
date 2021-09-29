package `in`.okcredit.merchant.customer_ui.ui.address

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface UpdateCustomerAddressContract {

    data class State(
        val customerId: String = "",
        val loading: Boolean = true,
        val currentAddress: String? = null,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SetLoading(val loading: Boolean) : PartialState()

        data class AddressChanged(val address: String) : PartialState()

        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        data class AddressChanged(val address: String) : Intent()

        object SubmitTapped : Intent()

        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object AddressUpdated : ViewEvent()
    }
}
