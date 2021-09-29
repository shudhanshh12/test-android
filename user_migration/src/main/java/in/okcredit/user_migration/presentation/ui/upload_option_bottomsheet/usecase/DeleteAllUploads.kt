package `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.usecase

import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRepo
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class DeleteAllUploads @Inject constructor(
    private val migrationRepo: Lazy<MigrationRepo>
) {
    fun execute(): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            migrationRepo.get().clearAllUploadFile()
        )
    }
}
