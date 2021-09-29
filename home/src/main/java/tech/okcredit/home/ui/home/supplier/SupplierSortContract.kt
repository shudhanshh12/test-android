package tech.okcredit.home.ui.home.supplier

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.accounting.contract.HomeSortType

object SupplierSortContract {

    data class State(
        val sortType: HomeSortType = HomeSortType.ACTIVITY
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        data class SortType(val type: HomeSortType) : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        data class SelectSortType(val type: HomeSortType) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object ApplySort : ViewEvent()
    }
}
