package `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface UploadOptionBottomSheetContract {
    data class State(
        val canShowUploadPdf: Boolean = true
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object DeleteAllUploads : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object TrackPdfEntryPointViewed : ViewEvent()
    }
}
