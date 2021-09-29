package `in`.okcredit.user_migration.presentation.ui.file_pick.usecase

import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRepo
import `in`.okcredit.fileupload.utils.AwsHelper
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetAllCanceledFiles @Inject constructor(
    private val migrationRepo: Lazy<MigrationRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<List<String>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            migrationRepo.get().getUploadStatus(businessId)
                .map {
                    val uploads =
                        it.filter { uploadStatus ->
                            uploadStatus.remoteUrl != "" &&
                                uploadStatus.status != AwsHelper.CANCELLED &&
                                uploadStatus.cancelled.not()
                        }
                    uploads.map { uploadStatus -> uploadStatus.filePath }
                }
        }
    }
}
