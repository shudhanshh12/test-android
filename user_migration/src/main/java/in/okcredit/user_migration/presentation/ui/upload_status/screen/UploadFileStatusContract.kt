package `in`.okcredit.user_migration.presentation.ui.upload_status.screen

import `in`.okcredit.fileupload.user_migration.domain.model.UploadStatus
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.user_migration.presentation.ui.upload_status.usecase.GetUploadedFilesUrl

interface UploadFileStatusContract {

    data class State(
        val isLoading: Boolean = true,
        val error: Boolean = false,
        val uploadStatus: List<UploadStatus> = arrayListOf(),
        val isEnabledSubmitButton: Boolean = false,
        val listOfUploadedFileUrls: List<String> = arrayListOf(),
        val listOfFileNames: List<String> = arrayListOf()
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class ShowLoading(val loading: Boolean) : PartialState()
        data class ShowError(val error: Boolean) : PartialState()
        data class ShowUploadStatus(val uploadStatus: List<UploadStatus>) : PartialState()
        data class SetFilesUrls(val response: GetUploadedFilesUrl.Response) : PartialState()
        data class CanEnabledSubmitButton(val canEnabled: Boolean) : PartialState()
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object SubmitButtonClicked : Intent()
        data class CancelUpload(val uploadStatus: UploadStatus) : Intent()
        data class RetryUpload(val uploadStatus: UploadStatus) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GoToDisplayParsedDataScreen : ViewEvent()
        object GoToBack : ViewEvent()
    }
}
