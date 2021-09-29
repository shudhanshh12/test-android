package `in`.okcredit.fileupload.user_migration.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FileUploadStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val filePath: String,
    val remoteUrl: String,
    val status: String,
    val uploadedSize: Long = 0L,
    val totalSize: Long = 0L,
    val color: Int,
    val percentage: Int,
    val cancelled: Boolean,
    @ColumnInfo(index = true) val businessId: String
)
