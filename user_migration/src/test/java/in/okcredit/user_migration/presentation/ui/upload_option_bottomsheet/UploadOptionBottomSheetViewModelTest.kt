package `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.user_migration.presentation.TestViewModel
import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.UploadOptionBottomSheetContract.*
import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.usecase.DeleteAllUploads
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test

class UploadOptionBottomSheetViewModelTest : TestViewModel<State, PartialState, ViewEvent>() {

    private val deleteAllUploads: DeleteAllUploads = mock()
    private val initialState = State()

    override fun createViewModel(): BaseViewModel<State, PartialState, ViewEvent> {
        return UploadOptionBottomSheetViewModel { deleteAllUploads }
    }

    @Test
    fun `Load Intent Should emit TrackPdfEntryPointViewed ViewEvent`() {

        pushIntent(Intent.Load)

        assertLastViewEvent(ViewEvent.TrackPdfEntryPointViewed)

        // assert state should not be changed
        assertLastState(initialState)
    }

    @Test
    fun `DeleteAllUploads Intent Should call the DeleteAllUploads usecase`() {

        whenever(deleteAllUploads.execute()).thenReturn(Observable.just(Result.Success(Unit)))

        pushIntent(Intent.DeleteAllUploads)

        verify(deleteAllUploads, times(1)).execute()
    }
}
