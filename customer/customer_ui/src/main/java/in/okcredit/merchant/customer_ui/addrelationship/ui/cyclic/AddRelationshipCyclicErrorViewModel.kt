package `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic

import `in`.okcredit.merchant.customer_ui.addrelationship.analytics.AddRelationshipEventTracker
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic.AddRelationshipCyclicError.Companion.CUSTOMER
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic.AddRelationshipCyclicErrorContract.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class AddRelationshipCyclicErrorViewModel @Inject constructor(
    initialState: State,
    private val tracker: Lazy<AddRelationshipEventTracker>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            observeMoveToSupplierIntent(),
            trackConflictDialogIntent()
        )
    }

    private fun trackConflictDialogIntent() = intent<Intent.Load>()
        .map {
            tracker.get().trackViewAddTransactionConflictDialog(
                type = getCurrentState().typeOfConflict,
                primaryCta = true,
                secondaryCTA = getCurrentState().canShowMoveCta,
                flow = "Add Relation",
                relation = if (getCurrentState().viewRelationshipType == CUSTOMER) "Customer" else "Supplier",
                source = getCurrentState().source,
                defaultMode = getCurrentState().defaultMode
            )
            PartialState.NoChange
        }

    private fun observeMoveToSupplierIntent() = intent<Intent.MoveToSupplier>()
        .map {
            emitViewEvent(ViewEvent.GotoMoveToSupplierFlow(getCurrentState().relationshipId))
            PartialState.NoChange
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
        }
    }
}
