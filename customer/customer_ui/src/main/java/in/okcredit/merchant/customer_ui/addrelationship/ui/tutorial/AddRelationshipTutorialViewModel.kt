package `in`.okcredit.merchant.customer_ui.addrelationship.ui.tutorial

import `in`.okcredit.merchant.customer_ui.addrelationship.ui.tutorial.AddRelationshipTutorialContract.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class AddRelationshipTutorialViewModel @Inject constructor(
    initialState: Lazy<State>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.empty()
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
        }
    }
}
