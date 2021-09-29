package `in`.okcredit.merchant.store.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("LongLine")
@Database(
    version = BusinessDatabase.DB_VERSION,
    entities = [Business::class, BusinessCategory::class, BusinessPreference::class, BusinessType::class]
)
@TypeConverters(DateTimeRoomCodec::class, CategoryRoomCodec::class, BusinessRoomCodec::class)
abstract class BusinessDatabase : RoomDatabase() {

    abstract fun merchantDataBaseDao(): BusinessDao

    companion object {
        const val DB_VERSION = 3
        const val DB_NAME = "okcredit-merchant.db"

        private var INSTANCE: BusinessDatabase? = null

        fun getInstance(context: Context): BusinessDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context, BusinessDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .build()
            }
            return INSTANCE!!
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS Business (`id` TEXT NOT NULL, `name` TEXT, `image_url` TEXT, `title` TEXT, `sub_title` TEXT, PRIMARY KEY(`id`))")
                database.execSQL("ALTER TABLE Merchant " + " ADD COLUMN business TEXT")
                database.execSQL("ALTER TABLE Category " + " ADD COLUMN isPopular INTEGER DEFAULT 1 not null")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                renameBusinessTableToBusinessType(database)
                renameCategoryToBusinessCategory(database)
                renameMerchantPreferenceTableToBusinessPreference(database)
                renameMerchantTableToBusiness(database)
                addIsFirstColumnToBusiness(database)
            }

            private fun renameBusinessTableToBusinessType(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Business RENAME TO BusinessType")
            }

            private fun renameCategoryToBusinessCategory(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Category RENAME TO BusinessCategory")
            }

            private fun renameMerchantPreferenceTableToBusinessPreference(database: SupportSQLiteDatabase) {
                // Create table
                database.execSQL("CREATE TABLE IF NOT EXISTS `BusinessPreference` (`businessId` TEXT NOT NULL, `key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`businessId`, `key`))")
                // Copy the data
                database.execSQL("INSERT INTO BusinessPreference (`businessId`, `key`, `value`) SELECT `merchantId`, `key`, `value` FROM MerchantPreference")
                // Drop old table
                database.execSQL("DROP TABLE MerchantPreference")
            }

            private fun renameMerchantTableToBusiness(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Merchant RENAME TO Business")
            }

            private fun addIsFirstColumnToBusiness(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Business ADD COLUMN isFirst INTEGER DEFAULT 0 not null")
            }
        }
    }
}
