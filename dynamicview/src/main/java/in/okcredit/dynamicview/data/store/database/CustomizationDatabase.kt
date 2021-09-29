package `in`.okcredit.dynamicview.data.store.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    version = CustomizationDatabase.DB_VERSION,
    entities = [CustomizationEntity::class]
)

abstract class CustomizationDatabase : RoomDatabase() {
    abstract fun customizationDatabaseDao(): CustomizationDatabaseDao

    companion object {
        const val DB_VERSION = 2
        const val DB_NAME = "okcredit-customization.db"

        internal var INSTANCE: CustomizationDatabase? = null

        fun getInstance(context: Context): CustomizationDatabase {
            if (INSTANCE == null) {
                synchronized(CustomizationDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, CustomizationDatabase::class.java, DB_NAME)
                            .addMigrations(migrate1to2())
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        private fun migrate1to2(): Migration =
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `CustomizationEntityV2` (`target` TEXT NOT NULL, `component` TEXT NOT NULL, `businessId` TEXT NOT NULL, PRIMARY KEY(`target`, `businessId`))")
                    database.execSQL("DROP TABLE IF EXISTS `CustomizationEntity`")
                }
            }
    }
}
