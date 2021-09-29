package `in`.okcredit.collection_ui.ui.passbook.add_to_khata

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface AddToKhataContract {

    data class State(
        val isLoading: Boolean = false,
        val source: String = "",
        val customer: Customer? = null,
        val collectionOnlinePayment: CollectionOnlinePayment? = null
    ) : UiState

    sealed class Intent : UserIntent {
        object Load : Intent()
        object TagCustomer : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetLoader(val isLoading: Boolean) : PartialState()
        data class SetCustomer(val customer: Customer) : PartialState()
        data class SetCollectionOnlinePayment(val collectionOnlinePayment: CollectionOnlinePayment) : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent {
        object OnSuccess : ViewEvent()
        data class OnError(val msg: String) : ViewEvent()
    }
}
