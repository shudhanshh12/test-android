package `in`.okcredit.fileupload.user_migration.data.database.room

import `in`.okcredit.fileupload.user_migration.data.database.model.FileUploadStatus
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface FileUploadDao {

    @Query("SELECT * FROM FileUploadStatus WHERE businessId = :businessId")
    fun listUploadStatus(businessId: String): Flowable<List<FileUploadStatus>>

    @Query("SELECT filePath FROM FileUploadStatus WHERE cancelled=0 and businessId = :businessId")
    fun notCancelledFilePath(businessId: String): Single<List<String>>

    @Query("SELECT status FROM FileUploadStatus WHERE businessId = :businessId")
    fun getOnlyStatus(businessId: String): Single<List<String>>

    @Query("SELECT filePath FROM FileUploadStatus WHERE cancelled=1 and businessId = :businessId")
    fun getCancelledFilePath(businessId: String): Flowable<List<String>>

    @Query("SELECT * FROM FileUploadStatus WHERE id=:id")
    fun getUploadStatus(id: Int): FileUploadStatus

    @Query("SELECT COUNT(id) FROM FileUploadStatus WHERE id=:id")
    fun getCount(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUploadStatus(vararg statusFiles: FileUploadStatus)

    @Query("DELETE FROM FileUploadStatus")
    fun clearAllUploadFile()

    @Query("UPDATE FileUploadStatus  SET cancelled=:cancelled where id=:id")
    fun clearUploadFile(id: Int, cancelled: Boolean)
}
