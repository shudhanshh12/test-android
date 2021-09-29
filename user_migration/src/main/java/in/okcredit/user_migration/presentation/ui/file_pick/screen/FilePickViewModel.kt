package `in`.okcredit.user_migration.presentation.ui.file_pick.screen

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.GetConnectionStatus
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.user_migration.R
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.FilePickContract.*
import `in`.okcredit.user_migration.presentation.ui.file_pick.usecase.GetAllCanceledFiles
import `in`.okcredit.user_migration.presentation.ui.file_pick.usecase.GetLocalFiles
import `in`.okcredit.user_migration.presentation.ui.file_pick.usecase.GetSelectedLocalFiles
import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.usecase.DeleteAllUploads
import dagger.Lazy
import io.reactivex.Observable
import java.io.File
import javax.inject.Inject

class FilePickViewModel @Inject constructor(
    initialState: State,
    private val getLocalFiles: Lazy<GetLocalFiles>,
    private val getSelectedLocalFiles: Lazy<GetSelectedLocalFiles>,
    private val checkNetwork: Lazy<GetConnectionStatus>,
    private val deleteAllUploads: Lazy<DeleteAllUploads>,
    private val getAllCancelFiles: Lazy<GetAllCanceledFiles>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    private val filesPath = mutableListOf<String>()
    private var selectedFileList = mutableListOf<String>()

    override fun handle(): Observable<out UiState.Partial<State>> {

        return Observable.mergeArray(
            loadFiles(),
            setSelectedFiles(),
            checkNetwork(),
            loadSearchFiles(),
            refreshFiles(),
            deleteStatusOfCacheFiles(),
            deselectCancelledFiles()
        )
    }

    private fun deselectCancelledFiles() = getAllCancelFiles.get().execute()
        .map {
            selectedFileList.clear()
            selectedFileList.addAll(it)
            PartialState.DeSelectCancelledFiles(it)
        }

    private fun loadFiles(): Observable<PartialState> {
        return getLocalFiles.get().execute()
            .map {
                when (it) {
                    is Result.Progress -> PartialState.ShowLoading(true)
                    is Result.Success -> {
                        if (it.value.isEmpty()) {
                            PartialState.SetNoFileFound
                        } else {
                            filesPath.addAll(it.value)
                            PartialState.ShowLocalFiles(filesPath)
                        }
                    }
                    else -> PartialState.ShowError(true)
                }
            }
    }

    private fun refreshFiles(): Observable<PartialState> {
        return intent<Intent.RefreshFiles>()
            .switchMap {
                getLocalFiles.get().execute()
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.ShowLoading(true)
                    is Result.Success -> {
                        if (it.value.isEmpty()) {
                            PartialState.SetNoFileFound
                        } else {
                            filesPath.addAll(it.value)
                            PartialState.ShowLocalFiles(filesPath)
                        }
                    }
                    else -> PartialState.ShowError(true)
                }
            }
    }

    private fun deleteStatusOfCacheFiles(): Observable<PartialState> {
        return intent<Intent.DeleteStatusAllCacheFiles>().map {
            deleteAllUploads.get().execute()
            PartialState.NoChange
        }
    }

    private fun loadSearchFiles(): Observable<PartialState> {
        return intent<Intent.SearchFiles>()
            .map {
                val filterFiles = filterFilesBySearchQuery(it.searchQuery)
                if (filterFiles.isEmpty()) {
                    PartialState.SetNoFileFound
                } else {
                    filesPath.clear()
                    filesPath.addAll(filterFiles)
                    PartialState.ShowLocalFiles(filterFiles)
                }
            }
    }

    private fun setSelectedFiles(): Observable<PartialState> {
        return intent<Intent.SetSelectedFile>().switchMap {
            getSelectedLocalFiles.get().execute(it.filePath, selectedFileList)
        }.map {
            when (it) {
                is Result.Progress -> PartialState.ShowLoading(true)
                is Result.Success -> PartialState.GetSelectedLocalFiles(it.value)
                else -> PartialState.ShowError(true)
            }
        }
    }

    private fun checkNetwork() = intent<Intent.CheckNetwork>()
        .switchMap {
            wrap(checkNetwork.get().executeWithTimeout())
        }
        .map {
            when (it) {
                is Result.Success -> {
                    if (it.value) {
                        emitViewEvent(ViewEvent.GotoUploadStatusScreen)
                    } else {
                        emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
                    }
                }
                is Result.Failure -> {
                    emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
                }
                else -> {
                }
            }
            PartialState.NoChange
        }

    private fun filterFilesBySearchQuery(searchQuery: String): List<String> {
        val finalList = mutableListOf<String>()
        finalList.addAll(filesPath.filter { File(it).name.toLowerCase().contains(searchQuery.toLowerCase()) })
        val rest = filesPath.minus(finalList)
        finalList.addAll(rest)
        return finalList
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.ShowLoading -> currentState.copy(
                isLoading = partialState.loading,
                error = false
            )
            is PartialState.ShowLocalFiles -> currentState.copy(
                noFileFound = false,
                deviceLocalFiles = partialState.files,
                isLoading = false,
                error = false
            )

            is PartialState.GetSelectedLocalFiles -> currentState.copy(
                selectedLocalFiles = partialState.response,
                isLoading = false,
                error = false
            )
            is PartialState.DeSelectCancelledFiles -> {
                currentState.copy(
                    selectedLocalFiles = GetSelectedLocalFiles.Response(
                        selectedFileList.isNotEmpty(),
                        selectedFileList
                    )
                )
            }

            is PartialState.ShowError -> currentState.copy(
                isLoading = false,
                error = true
            )
            is PartialState.NoChange -> currentState
            is PartialState.SetNoFileFound -> currentState.copy(
                noFileFound = true
            )
        }
    }
}
