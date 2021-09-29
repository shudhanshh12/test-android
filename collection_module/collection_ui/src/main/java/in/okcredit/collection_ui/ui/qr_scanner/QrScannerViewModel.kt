package `in`.okcredit.collection_ui.ui.qr_scanner

import `in`.okcredit.collection_ui.ui.qr_scanner.QRScannerContract.*
import `in`.okcredit.collection_ui.usecase.IsPermissionGranted
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import io.reactivex.Observable
import javax.inject.Inject

class QrScannerViewModel @Inject constructor(
    initialState: State,
    private val isPermissionGranted: IsPermissionGranted
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.SetTorchOn>()
                .map {
                    PartialState.SetTorchOn(it.isTorchOn)
                },

            intent<Intent.Load>()
                .switchMap { isPermissionGranted.execute(android.Manifest.permission.READ_EXTERNAL_STORAGE) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetStoragePermissionStatus(it.value)
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                }
        )
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetTorchOn -> currentState.copy(isTorchOn = partialState.isTorchOn)
            is PartialState.SetStoragePermissionStatus -> currentState.copy(
                isStoragePermissionGranted = partialState.isStoragePermissionGranted
            )
        }
    }
}
