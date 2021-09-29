package `in`.okcredit.merchant.customer_ui.addrelationship

import `in`.okcredit.merchant.customer_ui.addrelationship.AddRelationshipContract.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.permission.Permission
import javax.inject.Inject

class AddRelationshipViewModel @Inject constructor(
    initialState: State,
    private val context: Lazy<Context>,
    @ViewModelParam(AddRelationshipActivity.ARG_RELATIONSHIP_TYPE) private val relationshipType: Int,
    @ViewModelParam(AddRelationshipActivity.ARG_CAN_SHOW_TUTORIAL) private val canShowTutorial: Boolean,
    @ViewModelParam(AddRelationshipActivity.ARG_SHOW_MANUAL_FLOW) private val showManualFlow: Boolean,
    @ViewModelParam(AddRelationshipActivity.ARG_OPEN_FOR_RESULT) private val openForResult: Boolean,
    @ViewModelParam(AddRelationshipActivity.ARG_SOURCE) private val source: String,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            checkForContactPermission()
        )
    }

    private fun checkForContactPermission() = intent<Intent.Load>()
        .map {
            if (canShowTutorial) {
                emitViewEvent(ViewEvent.GoToAddRelationshipTutorial(relationshipType))
            } else {
                if (Permission.isContactPermissionAlreadyGranted(context.get()) && !showManualFlow) {
                    emitViewEvent(
                        ViewEvent.GoToAddRelationshipFromContacts(
                            relationshipType,
                            source = source,
                            openForResult = openForResult
                        )
                    )
                } else {
                    emitViewEvent(
                        ViewEvent.GoToAddRelationshipManually(
                            relationshipType,
                            source = source,
                            openForResult
                        )
                    )
                }
            }
            PartialState.NoChange
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
        }
    }
}
