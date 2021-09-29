package `in`.okcredit.fileupload.user_migration.data.repository

import `in`.okcredit.fileupload.user_migration.data.database.mapper.UserMigrationDBMapper
import `in`.okcredit.fileupload.user_migration.data.database.room.FileUploadDao
import `in`.okcredit.fileupload.user_migration.domain.model.UploadStatus
import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationLocalSource
import `in`.okcredit.fileupload.utils.AwsHelper
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.base.rxjava.SchedulerProvider
import javax.inject.Inject

class MigrationLocalSourceImpl @Inject constructor(
    private val fileUploadDao: FileUploadDao,
    private val userMigrationDbMapper: Lazy<UserMigrationDBMapper>,
    private val awsHelper: Lazy<AwsHelper>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
) : MigrationLocalSource {

    override fun saveUploadStatus(uploadStatus: UploadStatus, businessId: String): Completable {
        return Completable.fromAction {
            fileUploadDao.getCount(uploadStatus.id)
            if (uploadStatus.status == AwsHelper.COMPLETED) {
                val existing = fileUploadDao.getUploadStatus(uploadStatus.id)
                val updatedEntity = existing.copy(
                    status = awsHelper.get().getState(TransferState.COMPLETED),
                    color = awsHelper.get().getStateColor(TransferState.COMPLETED),
                    percentage = 100
                )
                fileUploadDao.insertUploadStatus(updatedEntity)
            } else {
                val entity = userMigrationDbMapper.get().mapDomainToDB(uploadStatus, businessId)
                fileUploadDao.insertUploadStatus(entity)
            }
        }.subscribeOn(schedulerProvider.get().io())
    }

    override fun updateUploadStatus(uploadStatus: UploadStatus, businessId: String): Completable {
        return Completable.fromAction {
            val entity = userMigrationDbMapper.get().mapDomainToDB(uploadStatus, businessId)
            fileUploadDao.insertUploadStatus(entity)
        }.subscribeOn(schedulerProvider.get().io())
    }

    override fun clearAllUploadFile(): Completable {
        return Completable.fromAction {
            fileUploadDao.clearAllUploadFile()
        }.subscribeOn(schedulerProvider.get().io())
    }

    override fun clearUploadFile(uploadStatus: UploadStatus?): Completable {
        return Completable.fromAction {
            fileUploadDao.clearUploadFile(uploadStatus!!.id, true)
        }.subscribeOn(schedulerProvider.get().io())
    }

    override fun getUploadStatus(businessId: String): Observable<List<UploadStatus>> {
        return fileUploadDao.listUploadStatus(businessId)
            .subscribeOn(schedulerProvider.get().io())
            .map { userMigrationDbMapper.get().mapDBToDomainList(it) }
            .toObservable()
    }

    override fun getOnlyUploadStatus(businessId: String): Single<List<String>> {
        return fileUploadDao.getOnlyStatus(businessId)
            .subscribeOn(schedulerProvider.get().io())
    }

    override fun notCancelledUploadedFileUrl(businessId: String): Single<List<String>> {
        return fileUploadDao.notCancelledFilePath(businessId)
            .subscribeOn(schedulerProvider.get().io())
    }

    override fun getCancelledUploadedFileUrl(businessId: String): Observable<List<String>> {
        return fileUploadDao.getCancelledFilePath(businessId)
            .subscribeOn(schedulerProvider.get().io())
            .toObservable()
    }
}
