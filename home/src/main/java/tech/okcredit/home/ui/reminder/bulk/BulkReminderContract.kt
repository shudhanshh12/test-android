package tech.okcredit.home.ui.reminder.bulk

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes

interface BulkReminderContract {

    data class State(
        val merchantId: String = "",
        val collectionAdopted: Boolean = false,
        val loading: Boolean = false,
        val bulkReminderList: List<BulkReminderItem> = emptyList(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        object Loading : PartialState()

        data class CustomerList(val list: List<Customer>) : PartialState()

        data class SelectCustomer(val customerId: String) : PartialState()

        data class SetBusiness(val id: String) : PartialState()

        data class SetCollectionActivated(val adopted: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        data class SubmitClicked(val selectedCustomersSet: List<String>) : Intent()

        data class SelectCustomer(val customerId: String) : Intent()

        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class ShowError(@StringRes val error: Int) : ViewEvent()

        object Success : ViewEvent()
    }
}
