package `in`.okcredit.user_migration.presentation.ui.upload_status.usecase

import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRepo
import `in`.okcredit.fileupload.utils.AwsHelper
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetUploadedFilesUrl @Inject constructor(
    private val migrationRepo: Lazy<MigrationRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<Result<Response>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                migrationRepo.get().getUploadStatus(businessId)
                    .map {
                        val completedUploads =
                            it.filter { it.remoteUrl != null && it.status == AwsHelper.COMPLETED && it.cancelled.not() }
                        val remoteUrls = completedUploads.map { it.remoteUrl }
                        val localUrls = completedUploads.map { it.filePath }
                        Response(remoteUrls, localUrls)
                    }
            }
        )
    }

    data class Response(
        val remoteUrls: List<String>,
        val localUrls: List<String>
    )
}
