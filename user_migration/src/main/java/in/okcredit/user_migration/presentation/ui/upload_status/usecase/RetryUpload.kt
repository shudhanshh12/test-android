package `in`.okcredit.user_migration.presentation.ui.upload_status.usecase

import `in`.okcredit.fileupload.user_migration.domain.model.UploadStatus
import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class RetryUpload @Inject constructor(
    private val migrationRepo: Lazy<MigrationRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(uploadStatus: UploadStatus): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                migrationRepo.get().retryUploadFile(uploadStatus, businessId)
            }
        )
    }
}
