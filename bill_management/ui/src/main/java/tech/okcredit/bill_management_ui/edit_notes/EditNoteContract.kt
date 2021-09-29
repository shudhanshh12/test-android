package tech.okcredit.bill_management_ui.edit_notes

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface EditNoteContract {

    data class State(
        val note: String? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetNote(val note: String) : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        data class EnteredMobileNumber(val enteredMobileNumber: String) : Intent()

        data class SubmitMobileNumber(val moblieNumber: String) : Intent()

        data class SetEditTextFocus(val hasFocus: Boolean) : Intent()

        data class EditedNote(val note: String) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object GoBack : ViewEvents()
    }
}
