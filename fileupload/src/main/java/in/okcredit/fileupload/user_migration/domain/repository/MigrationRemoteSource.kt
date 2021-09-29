package `in`.okcredit.fileupload.user_migration.domain.repository

import `in`.okcredit.fileupload.user_migration.domain.model.UploadStatus
import io.reactivex.Completable
import io.reactivex.Single

interface MigrationRemoteSource {

    fun uploadFile(filePath: List<String>, businessId: String): Completable

    fun uploadImage(imagePath: String, businessId: String): Single<String>

    fun retryUploadFile(uploadStatus: UploadStatus?, businessId: String): Completable

    fun cancelUploadFile(uploadStatus: UploadStatus?): Completable
}
