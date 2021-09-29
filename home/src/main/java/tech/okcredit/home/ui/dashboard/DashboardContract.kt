package tech.okcredit.home.ui.dashboard

import `in`.okcredit.dynamicview.data.model.Customization
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface DashboardContract {

    data class State(
        val isLoading: Boolean = true,
        val customization: Customization? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class DashboardCustomizationWithValues(val customization: Customization?) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent
}
