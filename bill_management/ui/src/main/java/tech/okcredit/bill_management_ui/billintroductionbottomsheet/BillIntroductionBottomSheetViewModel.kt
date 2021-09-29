package tech.okcredit.bill_management_ui.billintroductionbottomsheet

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.bill_management_ui.billintroductionbottomsheet.BillIntroductionBottomSheetContract.*
import tech.okcredit.bill_management_ui.billintroductionbottomsheet.BillIntroductionBottomSheetContract.PartialState.*
import javax.inject.Inject

class BillIntroductionBottomSheetViewModel @Inject constructor(
    initialState: Lazy<State>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .map { NoChange }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is NoChange -> currentState
        }
    }
}
