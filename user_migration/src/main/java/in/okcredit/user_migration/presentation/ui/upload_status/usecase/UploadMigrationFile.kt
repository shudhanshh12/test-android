package `in`.okcredit.user_migration.presentation.ui.upload_status.usecase

import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class UploadMigrationFile @Inject constructor(
    private val migrationRepo: Lazy<MigrationRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(selectedFilePath: List<String>): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            migrationRepo.get().uploadFile(selectedFilePath, businessId)
        }
    }
}
