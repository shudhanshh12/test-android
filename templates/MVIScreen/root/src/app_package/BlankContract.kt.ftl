package ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.base.BaseViewEvent

interface ${featureName}Contract {

    data class State(
        val isLoading: Boolean = true
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {}
}
