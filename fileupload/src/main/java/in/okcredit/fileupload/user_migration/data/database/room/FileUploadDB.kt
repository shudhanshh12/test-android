package `in`.okcredit.fileupload.user_migration.data.database.room

import `in`.okcredit.fileupload.user_migration.data.database.model.FileUploadStatus
import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(version = FileUploadDB.DB_VERSION, entities = [FileUploadStatus::class])
abstract class FileUploadDB : RoomDatabase() {
    abstract fun fileUploadsDataBaseDao(): FileUploadDao

    companion object {
        const val DB_VERSION = 2
        const val DB_NAME = "okcredit-file-upload.db"

        private var instance: FileUploadDB? = null

        @SuppressLint("SyntheticAccessor")
        fun getInstance(context: Context, migrationHandler: MultipleAccountsDatabaseMigrationHandler): FileUploadDB {
            if (instance == null) {
                synchronized(FileUploadDB::class.java) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(context, FileUploadDB::class.java, DB_NAME)
                            .addMigrations(migrate1to2(migrationHandler))
                            .build()
                    }
                }
            }
            return instance!!
        }

        private fun migrate1to2(migrationHandler: MultipleAccountsDatabaseMigrationHandler): Migration =
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    migrationHandler.execute(
                        database,
                        "`FileUploadStatus`"
                    )
                }
            }
    }
}
