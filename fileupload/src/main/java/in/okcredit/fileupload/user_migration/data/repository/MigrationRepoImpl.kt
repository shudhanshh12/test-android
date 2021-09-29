package `in`.okcredit.fileupload.user_migration.data.repository

import `in`.okcredit.fileupload.user_migration.domain.model.UploadStatus
import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationLocalSource
import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRemoteSource
import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRepo
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class MigrationRepoImpl @Inject constructor(
    private val migrationRemoteSource: Lazy<MigrationRemoteSource>,
    private val migrationLocalSource: Lazy<MigrationLocalSource>
) : MigrationRepo {

    override fun uploadFile(selectedFilePath: List<String>, businessId: String): Completable {

        return migrationLocalSource.get().notCancelledUploadedFileUrl(businessId)
            .map { nonCancelledUploadedFiles ->
                when {
                    nonCancelledUploadedFiles.isEmpty() -> selectedFilePath
                    selectedFilePath.isEmpty() -> emptyList()
                    else -> selectedFilePath.subtract(nonCancelledUploadedFiles)
                }.toList()
            }
            .flatMapCompletable {
                if (it.isNotEmpty()) {
                    migrationRemoteSource.get().uploadFile(it, businessId)
                } else {
                    Completable.complete()
                }
            }
    }

    override fun uploadImage(imagePath: String, businessId: String): Single<String> {
        return migrationRemoteSource.get().uploadImage(imagePath, businessId)
    }

    override fun clearAllUploadFile(): Completable {
        return migrationLocalSource.get().clearAllUploadFile()
    }

    override fun clearUploadFile(uploadStatus: UploadStatus?, businessId: String): Single<Boolean> {
        return migrationLocalSource.get().clearUploadFile(uploadStatus)
            .andThen(migrationRemoteSource.get().cancelUploadFile(uploadStatus))
            .andThen(isAllUploadsCancelled(businessId))
    }

    override fun retryUploadFile(uploadStatus: UploadStatus?, businessId: String): Completable {
        return migrationRemoteSource.get().retryUploadFile(uploadStatus, businessId)
    }

    override fun getUploadStatus(businessId: String): Observable<List<UploadStatus>> {
        return migrationLocalSource.get().getUploadStatus(businessId)
    }

    private fun isAllUploadsCancelled(businessId: String): Single<Boolean> {
        return migrationLocalSource.get().notCancelledUploadedFileUrl(businessId)
            .map { it.isEmpty() }
    }
}
