package tech.okcredit.sdk.store.database

import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    version = BillDatabase.DB_VERSION,
    entities = [DbBillDoc::class, DBBill::class, Account::class]
)
abstract class BillDatabase : RoomDatabase() {
    abstract fun coreDatabaseDao(): BillDatabaseDao

    companion object {
        const val DB_VERSION = 2
        const val DB_NAME = "okcredit-bill.db"

        internal var INSTANCE: BillDatabase? = null

        fun getInstance(context: Context, migrationHandler: MultipleAccountsDatabaseMigrationHandler): BillDatabase {
            if (INSTANCE == null) {

                synchronized(BillDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, BillDatabase::class.java, DB_NAME)
                            .addMigrations(migrate1To2(migrationHandler))
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        private fun migrate1To2(migrationHandler: MultipleAccountsDatabaseMigrationHandler): Migration =
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    migrationHandler.execute(
                        database = database,
                        tableName = "`DbBillDoc`"
                    )
                    migrationHandler.execute(
                        database = database,
                        tableName = "`DBBill`"
                    )
                    migrationHandler.execute(
                        database = database,
                        tableName = "`Account`"
                    )
                }
            }
    }
}
