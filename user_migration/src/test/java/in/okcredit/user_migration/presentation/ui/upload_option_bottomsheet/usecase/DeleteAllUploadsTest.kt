package `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.usecase

import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRepo
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import org.junit.Test

class DeleteAllUploadsTest {
    private val migrationRepo: MigrationRepo = mock()
    private val deleteAllUploads = DeleteAllUploads(Lazy { migrationRepo })

    @Test
    fun `return completable complete when deletedAllUploads`() {
        whenever(migrationRepo.clearAllUploadFile()).thenReturn(Completable.complete())
        val result = deleteAllUploads.execute().test()

        result.assertValues(
            Result.Progress(),
            Result.Success(Unit)
        )
    }
}
