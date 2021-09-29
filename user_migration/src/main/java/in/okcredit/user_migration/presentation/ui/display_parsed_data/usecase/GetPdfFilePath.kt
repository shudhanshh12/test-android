package `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase

import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class GetPdfFilePath @Inject constructor(
    private val migrationRepo: Lazy<MigrationRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(fileName: String): Single<Response> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            migrationRepo.get().getUploadStatus(businessId)
                .firstOrError()
                .map {
                    val status = it.find { uploadStatus ->
                        val file = File(uploadStatus.filePath)
                        file.name == fileName
                    }
                    if (status == null) {
                        Response(false, null)
                    } else {
                        val localFile = File(status.filePath)
                        if (localFile.exists()) {
                            Response(true, localFile)
                        } else {
                            Response(false, null)
                        }
                    }
                }
        }
    }

    data class Response(val found: Boolean, val file: File?)
}
