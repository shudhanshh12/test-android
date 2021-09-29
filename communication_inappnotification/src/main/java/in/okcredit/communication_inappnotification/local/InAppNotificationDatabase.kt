package `in`.okcredit.communication_inappnotification.local

import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    version = InAppNotificationDatabase.DB_VERSION,
    entities = [InAppNotification::class]
)
@TypeConverters(DisplayStatusMapper::class, ArrowOrientationMapper::class)
abstract class InAppNotificationDatabase : RoomDatabase() {
    abstract fun inAppNotificationDatabaseDao(): InAppNotificationDatabaseDao

    companion object {
        const val DB_VERSION = 2
        private const val DB_NAME = "okcredit-in-app-notification.db"

        internal var INSTANCE: InAppNotificationDatabase? = null

        fun getInstance(
            context: Context,
            migrationHandler: MultipleAccountsDatabaseMigrationHandler,
        ): InAppNotificationDatabase {
            if (INSTANCE == null) {
                synchronized(InAppNotificationDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, InAppNotificationDatabase::class.java, DB_NAME)
                            .addMigrations(migrate1to2(migrationHandler))
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        private fun migrate1to2(migrationHandler: MultipleAccountsDatabaseMigrationHandler): Migration =
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    migrationHandler.execute(
                        database,
                        "`InAppNotification`"
                    )
                }
            }
    }
}
