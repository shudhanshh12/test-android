package `in`.okcredit.user_migration.presentation.ui.upload_status.usecase

import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRepo
import `in`.okcredit.fileupload.utils.AwsHelper
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetSubmitButtonVisibility @Inject constructor(
    private val migrationRepo: Lazy<MigrationRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<Boolean> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            migrationRepo.get().getUploadStatus(businessId).map { listOfStatus ->
                listOfStatus.any { it.status == AwsHelper.COMPLETED }
            }
        }
    }
}
