package `in`.okcredit.shared.dialogs.bottomsheetloader

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderContract.*
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderContract.PartialState.*
import dagger.Lazy
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BottomSheetLoaderViewModel @Inject constructor(
    initialState: Lazy<State>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .map { ShowLoading },
            intent<Intent.Success>()
                .map { ShowSuccess },
            intent<Intent.Fail>()
                .map { ShowFailure },
            intent<Intent.Dismiss>()
                .delay(200, TimeUnit.MILLISECONDS)
                .map {
                    emitViewEvent(ViewEvent.Dismiss)
                    NoChange
                }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is NoChange -> currentState
            ShowLoading -> currentState.copy(isLoading = true)
            ShowSuccess -> currentState.copy(isLoading = false, result = true)
            ShowFailure -> currentState.copy(isLoading = false, result = false)
        }
    }
}
