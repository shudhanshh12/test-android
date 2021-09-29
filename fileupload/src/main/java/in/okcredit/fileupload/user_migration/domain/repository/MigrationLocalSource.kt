package `in`.okcredit.fileupload.user_migration.domain.repository

import `in`.okcredit.fileupload.user_migration.domain.model.UploadStatus
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface MigrationLocalSource {

    fun saveUploadStatus(uploadStatus: UploadStatus, businessId: String): Completable

    fun updateUploadStatus(uploadStatus: UploadStatus, businessId: String): Completable

    fun clearAllUploadFile(): Completable

    fun clearUploadFile(uploadStatus: UploadStatus?): Completable

    fun getUploadStatus(businessId: String): Observable<List<UploadStatus>>

    fun getOnlyUploadStatus(businessId: String): Single<List<String>>

    fun notCancelledUploadedFileUrl(businessId: String): Single<List<String>>

    fun getCancelledUploadedFileUrl(businessId: String): Observable<List<String>>
}
