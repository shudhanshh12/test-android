package `in`.okcredit.fileupload.user_migration.data.database.mapper

import `in`.okcredit.fileupload.user_migration.domain.model.UploadStatus
import javax.inject.Inject
import `in`.okcredit.fileupload.user_migration.data.database.model.FileUploadStatus as UploadStatusDB

class UserMigrationDBMapper @Inject constructor() {

    fun mapDomainToDB(domainUploadStatus: UploadStatus, businessId: String): UploadStatusDB {
        return UploadStatusDB(
            id = domainUploadStatus.id,
            filePath = domainUploadStatus.filePath,
            remoteUrl = domainUploadStatus.remoteUrl,
            status = domainUploadStatus.status,
            uploadedSize = domainUploadStatus.uploadedSize,
            totalSize = domainUploadStatus.totalSize,
            color = domainUploadStatus.color,
            percentage = domainUploadStatus.percentage,
            cancelled = domainUploadStatus.cancelled,
            businessId = businessId
        )
    }

    fun mapDBToDomainList(dbUploadStatus: List<UploadStatusDB>): List<UploadStatus> =
        dbUploadStatus.map { it.mapDbToDomain() }
}

fun UploadStatusDB.mapDbToDomain() = UploadStatus(
    id = this.id,
    filePath = this.filePath,
    remoteUrl = this.remoteUrl,
    status = this.status,
    uploadedSize = this.uploadedSize,
    totalSize = this.totalSize,
    color = this.color,
    percentage = this.percentage,
    cancelled = this.cancelled
)
