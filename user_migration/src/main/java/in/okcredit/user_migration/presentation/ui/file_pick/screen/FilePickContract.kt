package `in`.okcredit.user_migration.presentation.ui.file_pick.screen

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.user_migration.presentation.ui.file_pick.usecase.GetSelectedLocalFiles.*

interface FilePickContract {

    data class State(
        val isLoading: Boolean = true,
        val error: Boolean = false,
        val deviceLocalFiles: List<String> = arrayListOf(),
        val noFileFound: Boolean = false,
        val selectedLocalFiles: Response = Response(false, arrayListOf())
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class ShowLoading(val loading: Boolean) : PartialState()
        data class ShowError(val error: Boolean) : PartialState()
        object NoChange : PartialState()
        object SetNoFileFound : PartialState()
        data class ShowLocalFiles(val files: List<String>) : PartialState()
        data class DeSelectCancelledFiles(val files: List<String>) : PartialState()
        data class GetSelectedLocalFiles(val response: Response) :
            PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object CheckNetwork : Intent()
        object RefreshFiles : Intent()
        object DeleteStatusAllCacheFiles : Intent()
        data class SearchFiles(val searchQuery: String) : Intent()
        data class SetSelectedFile(val filePath: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GotoUploadStatusScreen : ViewEvent()
        data class ShowError(val error: Int) : ViewEvent()
    }
}
