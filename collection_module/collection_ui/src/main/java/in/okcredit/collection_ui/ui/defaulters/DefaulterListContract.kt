package `in`.okcredit.collection_ui.ui.defaulters

import `in`.okcredit.collection_ui.ui.defaulters.model.Defaulter
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface DefaulterListContract {

    data class State(
        val isLoading: Boolean = true,
        val defaulterList: List<Defaulter>? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class DefaulterList(val defaulterList: List<Defaulter>) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent
}
