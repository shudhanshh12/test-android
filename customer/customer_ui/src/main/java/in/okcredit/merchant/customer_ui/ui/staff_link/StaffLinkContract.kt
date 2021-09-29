package `in`.okcredit.merchant.customer_ui.ui.staff_link

import `in`.okcredit.merchant.customer_ui.usecase.GetCollectionStaffLinkScreen
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface StaffLinkContract {

    data class State(
        val loading: Boolean = true,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SetLoading(val loading: Boolean) : PartialState()
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class MoveToScreen(val staffLinkScreen: GetCollectionStaffLinkScreen.StaffLinkScreen) : ViewEvent()
    }
}
