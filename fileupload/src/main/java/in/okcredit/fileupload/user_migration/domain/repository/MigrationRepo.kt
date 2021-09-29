package `in`.okcredit.fileupload.user_migration.domain.repository

import `in`.okcredit.fileupload.user_migration.domain.model.UploadStatus
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface MigrationRepo {

    fun uploadFile(filePath: List<String>, businessId: String): Completable

    fun uploadImage(imagePath: String, businessId: String): Single<String>

    fun clearAllUploadFile(): Completable

    fun clearUploadFile(uploadStatus: UploadStatus?, businessId: String): Single<Boolean>

    fun retryUploadFile(uploadStatus: UploadStatus?, businessId: String): Completable

    fun getUploadStatus(businessId: String): Observable<List<UploadStatus>>
}
