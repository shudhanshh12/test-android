package merchant.okcredit.user_stories.store.database

import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import `in`.okcredit.shared.store.database.TimestampConverter
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    version = UserStoriesDatabase.DB_VERSION,
    entities = [MyStory::class, OthersStory::class],
    exportSchema = false
)
@TypeConverters(TimestampConverter::class)
abstract class UserStoriesDatabase : RoomDatabase() {
    abstract fun userStoriesDao(): UserStoriesDao

    companion object {
        const val DB_VERSION = 2
        const val DB_NAME = "okcredit-userstories.db"

        internal var INSTANCE: UserStoriesDatabase? = null

        fun getInstance(
            context: Context,
            multipleAccountMigrationHandler: MultipleAccountsDatabaseMigrationHandler,
        ): UserStoriesDatabase {
            if (INSTANCE == null) {
                synchronized(UserStoriesDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, UserStoriesDatabase::class.java, DB_NAME)
                            .addMigrations(migrate1to2(multipleAccountMigrationHandler))
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        private fun migrate1to2(multipleAccountMigrationHandler: MultipleAccountsDatabaseMigrationHandler): Migration =
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    multipleAccountMigrationHandler.execute(
                        database,
                        "`MyStory`"
                    )
                    multipleAccountMigrationHandler.execute(
                        database,
                        "`OthersStory`"
                    )
                }
            }
    }
}
