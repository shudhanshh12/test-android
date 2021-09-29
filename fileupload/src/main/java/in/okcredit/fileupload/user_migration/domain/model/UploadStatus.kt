package `in`.okcredit.fileupload.user_migration.domain.model

data class UploadStatus(
    val id: Int,
    val filePath: String = "",
    val remoteUrl: String = "",
    val status: String,
    val uploadedSize: Long = 0L,
    val totalSize: Long = 0L,
    val color: Int,
    val percentage: Int,
    val cancelled: Boolean
)
