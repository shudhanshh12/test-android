package `in`.okcredit.merchant.customer_ui.data.local.db

import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    version = CustomerDatabase.DB_VERSION,
    entities = [
        CustomerAdditionalInfo::class,
    ]
)
abstract class CustomerDatabase : RoomDatabase() {

    abstract fun customerDatabaseDao(): CustomerDatabaseDao

    companion object {
        const val DB_VERSION = 2
        const val DB_NAME = "okcredit_customer.db"

        internal var INSTANCE: CustomerDatabase? = null

        fun getInstance(
            context: Context,
            migrationHandler: MultipleAccountsDatabaseMigrationHandler,
        ): CustomerDatabase {
            if (INSTANCE == null) {
                synchronized(CustomerDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, CustomerDatabase::class.java, DB_NAME)
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
                        "`CustomerAdditionalInfo`"
                    )
                }
            }
    }
}
