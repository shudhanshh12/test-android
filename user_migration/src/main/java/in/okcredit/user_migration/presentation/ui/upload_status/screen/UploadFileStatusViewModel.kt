package `in`.okcredit.user_migration.presentation.ui.upload_status.screen

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.user_migration.presentation.ui.upload_status.usecase.CancelUpload
import `in`.okcredit.user_migration.presentation.ui.upload_status.usecase.GetSubmitButtonVisibility
import `in`.okcredit.user_migration.presentation.ui.upload_status.usecase.GetUploadedFileStatus
import `in`.okcredit.user_migration.presentation.ui.upload_status.usecase.GetUploadedFilesUrl
import `in`.okcredit.user_migration.presentation.ui.upload_status.usecase.RetryUpload
import `in`.okcredit.user_migration.presentation.ui.upload_status.usecase.UploadMigrationFile
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class UploadFileStatusViewModel @Inject constructor(
    initialState: UploadFileStatusContract.State,
    private val uploadMigrationFile: Lazy<UploadMigrationFile>,
    private val getUploadedFileStatus: Lazy<GetUploadedFileStatus>,
    private val getSubmitButtonVisibility: Lazy<GetSubmitButtonVisibility>,
    private val getUploadedFilesUrl: Lazy<GetUploadedFilesUrl>,
    private val cancelUpload: Lazy<CancelUpload>,
    private val retryUpload: Lazy<RetryUpload>,
    private val args: Lazy<UploadFileStatusFragmentArgs>
) : BaseViewModel<UploadFileStatusContract.State, UploadFileStatusContract.PartialState, UploadFileStatusContract.ViewEvent>(
    initialState
) {

    override fun handle(): Observable<out UiState.Partial<UploadFileStatusContract.State>> {
        return Observable.mergeArray(

            uploadSelectedFiles(),

            getUploadingFileStatus(),

            setSubmitVisibility(),

            cancelUpload(),

            retryUpload(),

            getUploadedFileUrls(),

            setSubmitClicked(),
        )
    }

    private fun uploadSelectedFiles(): Observable<UploadFileStatusContract.PartialState>? {
        return intent<UploadFileStatusContract.Intent.Load>()
            .switchMap {
                wrap(uploadMigrationFile.get().execute(args.get().listOfFiles.list))
            }.map {
                when (it) {
                    is Result.Progress -> UploadFileStatusContract.PartialState.ShowLoading(true)
                    is Result.Success -> UploadFileStatusContract.PartialState.NoChange
                    else -> UploadFileStatusContract.PartialState.ShowError(true)
                }
            }
    }

    private fun getUploadingFileStatus(): Observable<UploadFileStatusContract.PartialState>? {
        return getUploadedFileStatus.get().execute()
            .map {
                when (it) {
                    is Result.Progress -> UploadFileStatusContract.PartialState.ShowLoading(true)
                    is Result.Success -> {
                        UploadFileStatusContract.PartialState.ShowUploadStatus(it.value)
                    }
                    else -> UploadFileStatusContract.PartialState.ShowError(true)
                }
            }
    }

    private fun setSubmitClicked(): Observable<UploadFileStatusContract.PartialState.NoChange>? {
        return intent<UploadFileStatusContract.Intent.SubmitButtonClicked>().map {
            emitViewEvent(UploadFileStatusContract.ViewEvent.GoToDisplayParsedDataScreen)
            UploadFileStatusContract.PartialState.NoChange
        }
    }

    private fun getUploadedFileUrls(): Observable<UploadFileStatusContract.PartialState>? {
        return getUploadedFilesUrl.get().execute()
            .map {
                when (it) {
                    is Result.Progress -> UploadFileStatusContract.PartialState.ShowLoading(true)
                    is Result.Success -> UploadFileStatusContract.PartialState.SetFilesUrls(it.value)

                    is Result.Failure -> UploadFileStatusContract.PartialState.ShowError(true)
                }
            }
    }

    private fun retryUpload(): Observable<UploadFileStatusContract.PartialState> {
        return intent<UploadFileStatusContract.Intent.RetryUpload>()
            .switchMap { retryUpload.get().execute(it.uploadStatus) }
            .map {
                when (it) {
                    is Result.Success -> {
                        UploadFileStatusContract.PartialState.CanEnabledSubmitButton(true)
                    }
                    else -> UploadFileStatusContract.PartialState.NoChange
                }
            }
    }

    private fun cancelUpload(): Observable<UploadFileStatusContract.PartialState> {
        return intent<UploadFileStatusContract.Intent.CancelUpload>()
            .switchMap { cancelUpload.get().execute(it.uploadStatus) }
            .map {
                when (it) {
                    is Result.Success -> {
                        if (it.value) {
                            emitViewEvent(UploadFileStatusContract.ViewEvent.GoToBack)
                        }
                        UploadFileStatusContract.PartialState.NoChange
                    }
                    else -> UploadFileStatusContract.PartialState.NoChange
                }
            }
    }

    private fun setSubmitVisibility(): Observable<UploadFileStatusContract.PartialState.CanEnabledSubmitButton>? {
        return intent<UploadFileStatusContract.Intent.Load>()
            .switchMap {
                getSubmitButtonVisibility.get().execute()
            }.map {
                UploadFileStatusContract.PartialState.CanEnabledSubmitButton(it)
            }
    }

    override fun reduce(
        currentState: UploadFileStatusContract.State,
        partialState: UploadFileStatusContract.PartialState
    ): UploadFileStatusContract.State {
        return when (partialState) {
            is UploadFileStatusContract.PartialState.NoChange -> currentState
            is UploadFileStatusContract.PartialState.ShowLoading -> currentState.copy(
                isLoading = partialState.loading,
                error = false
            )
            is UploadFileStatusContract.PartialState.ShowUploadStatus -> currentState.copy(
                uploadStatus = partialState.uploadStatus,
                isLoading = false,
                error = false
            )

            is UploadFileStatusContract.PartialState.ShowError -> currentState.copy(
                isLoading = false,
                error = true
            )
            is UploadFileStatusContract.PartialState.CanEnabledSubmitButton -> currentState.copy(
                isEnabledSubmitButton = partialState.canEnabled
            )
            is UploadFileStatusContract.PartialState.SetFilesUrls -> currentState.copy(
                listOfUploadedFileUrls = partialState.response.remoteUrls,
                listOfFileNames = partialState.response.localUrls
            )
        }
    }
}
