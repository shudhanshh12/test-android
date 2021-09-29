package `in`.okcredit.collection_ui.ui.benefits

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface CollectionsBenefitContract {

    data class State(
        val sendReminder: Boolean = false,
        val customerId: String? = null,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object SetupClicked : Intent()
        object SendReminder : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object ShowAddBankDetails : ViewEvent()
        data class SendReminder(val intent: android.content.Intent) : ViewEvent()
    }
}
