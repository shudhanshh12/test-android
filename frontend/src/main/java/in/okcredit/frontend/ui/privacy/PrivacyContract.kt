package `in`.okcredit.frontend.ui.privacy

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface PrivacyContract {

    data class State(
        val loader: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        // show alert
        object Load : Intent()
    }

    interface Navigator {
        fun gotoLogin()
    }
}
