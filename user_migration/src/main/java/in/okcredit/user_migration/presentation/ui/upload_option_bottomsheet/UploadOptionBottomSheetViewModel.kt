package `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.UploadOptionBottomSheetContract.*
import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.usecase.DeleteAllUploads
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class UploadOptionBottomSheetViewModel @Inject constructor(
    private val deleteAllUploads: Lazy<DeleteAllUploads>
) : BaseViewModel<State, PartialState, ViewEvent>(
    State()
) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            deleteAllUploads(),
            trackEntryPointViewed()
        )
    }

    private fun deleteAllUploads(): Observable<PartialState.NoChange> {
        return intent<Intent.DeleteAllUploads>()
            .switchMap { deleteAllUploads.get().execute() }
            .map {
                PartialState.NoChange
            }
    }

    private fun trackEntryPointViewed() = intent<Intent.Load>()
        .map {
            emitViewEvent(ViewEvent.TrackPdfEntryPointViewed)
            PartialState.NoChange
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
        }
    }
}
