package `in`.okcredit.collection_ui.ui.home.adoption

import `in`.okcredit.collection_ui.ui.home.adoption.CollectionAdoptionV2Contract.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import io.reactivex.Observable
import javax.inject.Inject

class CollectionAdoptionV2ViewModel @Inject constructor(
    initialState: State,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            observeSetupClicked(),
        )
    }

    private fun observeSetupClicked() = intent<Intent.SetupClicked>()
        .map {
            emitViewEvent(ViewEvent.GoToAddDestination(getCurrentState().referredByMerchantId))
            PartialState.NoChange
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
        }
    }
}
