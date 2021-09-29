package `in`.okcredit.frontend.ui.sync

import `in`.okcredit.accounting_core.contract.SyncState
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import org.jetbrains.annotations.NonNls

interface SyncContract {

    companion object {
        @NonNls
        const val KEY_SKIP_SELECT_BUSINESS_SCREEN = "skip_select_business_screen"
    }

    data class State(
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val taskProgress: Int = 0,
        val isSyncRetryVisible: Boolean = false,
        val syncState: SyncState = SyncState.WAITING,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        data class SetErrorState(val error: Boolean) : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        data class ShowNoInternet(val isSyncRetryVisible: Boolean) : PartialState()

        data class SetDataProgress(val syncState: SyncState) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        object ClearNetworkError : PartialState()

        data class SetVisibilityOfRetry(val status: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        // retry sync
        object Retry : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        object GoToSelectBusinessOrHome : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GotoLogin : ViewEvent()

        object GoHome : ViewEvent()

        object GoToSelectBusinessOrHome : ViewEvent()
    }
}
