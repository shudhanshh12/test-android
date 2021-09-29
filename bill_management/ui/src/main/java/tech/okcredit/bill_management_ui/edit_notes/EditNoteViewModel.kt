package tech.okcredit.bill_management_ui.edit_notes

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bill_management_ui.edit_notes.EditNoteContract.*
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import javax.inject.Inject

class EditNoteViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam(BILL_INTENT_EXTRAS.NOTE) val note: String,
    @ViewModelParam(BILL_INTENT_EXTRAS.BILL_ID) val billId: String,
    private val updateNote: Lazy<UpdateNote>
) : BaseViewModel<State, PartialState, ViewEvents>(initialState) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(

            intent<Intent.Load>()
                .map {
                    PartialState.SetNote(note)
                },
            intent<Intent.EditedNote>()
                .switchMap {
                    updateNote.get().execute(UpdateNote.Request(billId, it.note, note))
                }.map {
                    when (it) {
                        is Result.Success -> {
                            emitViewEvent(ViewEvents.GoBack)
                            PartialState.NoChange
                        }
                        else -> PartialState.NoChange
                    }
                }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetNote -> currentState.copy(note = partialState.note)
        }
    }
}
