package `in`.okcredit.merchant.suppliercredit.store.database

import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    version = SupplierDataBase.DB_VERSION,
    entities = [Supplier::class, Transaction::class, NotificationReminder::class],
    views = [SupplierWithTransactionsInfo::class]
)
@TypeConverters(DateTimeRoomCodec::class)
abstract class SupplierDataBase : RoomDatabase() {

    abstract fun supplierDataBaseDao(): SupplierDataBaseDao

    companion object {
        const val DB_VERSION = 8
        internal const val DB_NAME = "okcredit-suppliercredit.db"

        internal var INSTANCE: SupplierDataBase? = null

        fun getInstance(
            context: Context,
            migrationHandler: MultipleAccountsDatabaseMigrationHandler,
        ): SupplierDataBase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, SupplierDataBase::class.java, DB_NAME)
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        migration7To8(migrationHandler)
                    )
                    .build()
            }
            return INSTANCE!!
        }

        internal val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(_db: SupportSQLiteDatabase) {

                _db.execSQL("ALTER TABLE Supplier ADD COLUMN addTransactionRestricted INTEGER DEFAULT 0 not null")

                _db.execSQL("DROP VIEW IF EXISTS `SupplierWithTransactionsInfo`")

                _db.execSQL(
                    "CREATE VIEW `SupplierWithTransactionsInfo` AS SELECT\n" +
                        "            supplier.id,\n" +
                        "            supplier.registered,\n" +
                        "            supplier.deleted,\n" +
                        "            supplier.createTime,\n" +
                        "            supplier.txnStartTime,\n" +
                        "            supplier.name,\n" +
                        "            supplier.mobile,\n" +
                        "            supplier.address,\n" +
                        "            supplier.profileImage,\n" +
                        "            SUM(CASE WHEN `TRANSACTION`.deleted == 1 THEN 0 WHEN `TRANSACTION`.payment == 0 THEN -1 * `TRANSACTION`.amount ELSE `TRANSACTION`.amount END) as balance,\n" +
                        "            SUM(CASE WHEN supplier.lastViewTime == 0 THEN 0 WHEN `TRANSACTION`.updateTime > supplier.lastViewTime AND `TRANSACTION`.createdBySupplier == 1 AND `TRANSACTION`.deleted == 0  THEN 1 ELSE 0 END) as newActivityCount,\n" +
                        "            MAX(`TRANSACTION`.updateTime ) as lastActivityTime,\n" +
                        "            supplier.lastViewTime,\n" +
                        "            supplier.txnAlertEnabled,\n" +
                        "            supplier.lang,\n" +
                        "            MAX(`TRANSACTION`.syncing ) as syncing,\n" +
                        "            MAX(`TRANSACTION`.lastSyncTime ) as lastSyncTime,\n" +
                        "            supplier.addTransactionRestricted\n" +
                        "        FROM Supplier\n" +
                        "        LEFT OUTER JOIN `TRANSACTION` ON Supplier.id = `TRANSACTION`.supplierId\n" +
                        "        GROUP BY Supplier.id"
                )
            }
        }

        internal val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(_db: SupportSQLiteDatabase) {

                _db.execSQL("ALTER TABLE Supplier ADD COLUMN state INTEGER DEFAULT 1 not null")
                _db.execSQL("ALTER TABLE Supplier ADD COLUMN blockedBySupplier INTEGER DEFAULT 0 not null")

                _db.execSQL("DROP VIEW IF EXISTS `SupplierWithTransactionsInfo`")

                _db.execSQL(
                    "CREATE VIEW `SupplierWithTransactionsInfo` AS SELECT\n" +
                        "            supplier.id,\n" +
                        "            supplier.registered,\n" +
                        "            supplier.deleted,\n" +
                        "            supplier.createTime,\n" +
                        "            supplier.txnStartTime,\n" +
                        "            supplier.name,\n" +
                        "            supplier.mobile,\n" +
                        "            supplier.address,\n" +
                        "            supplier.profileImage,\n" +
                        "            SUM(CASE WHEN `TRANSACTION`.deleted == 1 THEN 0 WHEN `TRANSACTION`.payment == 0 THEN -1 * `TRANSACTION`.amount ELSE `TRANSACTION`.amount END) as balance,\n" +
                        "            SUM(CASE WHEN supplier.lastViewTime == 0 THEN 0 WHEN `TRANSACTION`.updateTime > supplier.lastViewTime AND `TRANSACTION`.createdBySupplier == 1 AND `TRANSACTION`.deleted == 0  THEN 1 ELSE 0 END) as newActivityCount,\n" +
                        "            MAX(`TRANSACTION`.updateTime ) as lastActivityTime,\n" +
                        "            supplier.lastViewTime,\n" +
                        "            supplier.txnAlertEnabled,\n" +
                        "            supplier.lang,\n" +
                        "            MAX(`TRANSACTION`.syncing ) as syncing,\n" +
                        "            MAX(`TRANSACTION`.lastSyncTime ) as lastSyncTime,\n" +
                        "            supplier.addTransactionRestricted,\n" +
                        "            supplier.state,\n" +
                        "            supplier.blockedBySupplier\n" +
                        "        FROM Supplier\n" +
                        "        LEFT OUTER JOIN `TRANSACTION` ON Supplier.id = `TRANSACTION`.supplierId\n" +
                        "        GROUP BY Supplier.id"
                )
            }
        }
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("ALTER TABLE Supplier ADD COLUMN restrictContactSync INTEGER DEFAULT 0 not null")

                _db.execSQL("DROP VIEW IF EXISTS `SupplierWithTransactionsInfo`")

                _db.execSQL(
                    "CREATE VIEW `SupplierWithTransactionsInfo` AS SELECT\n" +
                        "            supplier.id,\n" +
                        "            supplier.registered,\n" +
                        "            supplier.deleted,\n" +
                        "            supplier.createTime,\n" +
                        "            supplier.txnStartTime,\n" +
                        "            supplier.name,\n" +
                        "            supplier.mobile,\n" +
                        "            supplier.address,\n" +
                        "            supplier.profileImage,\n" +
                        "            supplier.restrictContactSync,\n" +
                        "            SUM(CASE WHEN `TRANSACTION`.deleted == 1 THEN 0 WHEN `TRANSACTION`.payment == 0 THEN -1 * `TRANSACTION`.amount ELSE `TRANSACTION`.amount END) as balance,\n" +
                        "            SUM(CASE WHEN supplier.lastViewTime == 0 THEN 0 WHEN `TRANSACTION`.updateTime > supplier.lastViewTime AND `TRANSACTION`.createdBySupplier == 1 AND `TRANSACTION`.deleted == 0  THEN 1 ELSE 0 END) as newActivityCount,\n" +
                        "            MAX(`TRANSACTION`.updateTime ) as lastActivityTime,\n" +
                        "            supplier.lastViewTime,\n" +
                        "            supplier.txnAlertEnabled,\n" +
                        "            supplier.lang,\n" +
                        "            MAX(`TRANSACTION`.syncing ) as syncing,\n" +
                        "            MAX(`TRANSACTION`.lastSyncTime ) as lastSyncTime,\n" +
                        "            supplier.addTransactionRestricted,\n" +
                        "            supplier.state,\n" +
                        "            supplier.blockedBySupplier\n" +
                        "        FROM Supplier\n" +
                        "        LEFT OUTER JOIN `TRANSACTION` ON Supplier.id = `TRANSACTION`.supplierId\n" +
                        "        GROUP BY Supplier.id"
                )
            }
        }
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("ALTER TABLE `Transaction` ADD COLUMN transactionState INTEGER DEFAULT -1 NOT NULL")

                _db.execSQL("DROP VIEW IF EXISTS `SupplierWithTransactionsInfo`")

                _db.execSQL(
                    "CREATE VIEW `SupplierWithTransactionsInfo` AS SELECT\n" +
                        "            supplier.id,\n" +
                        "            supplier.registered,\n" +
                        "            supplier.deleted,\n" +
                        "            supplier.createTime,\n" +
                        "            supplier.txnStartTime,\n" +
                        "            supplier.name,\n" +
                        "            supplier.mobile,\n" +
                        "            supplier.address,\n" +
                        "            supplier.profileImage,\n" +
                        "            supplier.restrictContactSync,\n" +
                        "            SUM(CASE WHEN `TRANSACTION`.deleted == 1 THEN 0 WHEN `Transaction`.transactionState == 0 THEN 0 WHEN `TRANSACTION`.payment == 0 THEN -1 * `TRANSACTION`.amount ELSE `TRANSACTION`.amount END) as balance,\n" +
                        "            SUM(CASE WHEN supplier.lastViewTime == 0 THEN 0 WHEN `TRANSACTION`.updateTime > supplier.lastViewTime AND `TRANSACTION`.createdBySupplier == 1 AND `TRANSACTION`.deleted == 0  THEN 1 ELSE 0 END) as newActivityCount,\n" +
                        "            MAX(`TRANSACTION`.updateTime ) as lastActivityTime,\n" +
                        "            supplier.lastViewTime,\n" +
                        "            supplier.txnAlertEnabled,\n" +
                        "            supplier.lang,\n" +
                        "            MAX(`TRANSACTION`.syncing ) as syncing,\n" +
                        "            MAX(`TRANSACTION`.lastSyncTime ) as lastSyncTime,\n" +
                        "            supplier.addTransactionRestricted,\n" +
                        "            supplier.state,\n" +
                        "            supplier.blockedBySupplier\n" +
                        "        FROM Supplier\n" +
                        "        LEFT OUTER JOIN `TRANSACTION` ON Supplier.id = `TRANSACTION`.supplierId\n" +
                        "        GROUP BY Supplier.id"
                )
            }
        }

        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("ALTER TABLE `Transaction` ADD COLUMN txCategory INTEGER DEFAULT 0 NOT NULL")

                _db.execSQL("DROP VIEW IF EXISTS `SupplierWithTransactionsInfo`")

                _db.execSQL(
                    "CREATE VIEW `SupplierWithTransactionsInfo` AS SELECT\n" +
                        "            supplier.id,\n" +
                        "            supplier.registered,\n" +
                        "            supplier.deleted,\n" +
                        "            supplier.createTime,\n" +
                        "            supplier.txnStartTime,\n" +
                        "            supplier.name,\n" +
                        "            supplier.mobile,\n" +
                        "            supplier.address,\n" +
                        "            supplier.profileImage,\n" +
                        "            supplier.restrictContactSync,\n" +
                        "            SUM(CASE WHEN `TRANSACTION`.deleted == 1 THEN 0 WHEN `Transaction`.transactionState == 0 THEN 0 WHEN `TRANSACTION`.payment == 0 THEN -1 * `TRANSACTION`.amount ELSE `TRANSACTION`.amount END) as balance,\n" +
                        "            SUM(CASE WHEN supplier.lastViewTime == 0 THEN 0 WHEN `TRANSACTION`.updateTime > supplier.lastViewTime AND `TRANSACTION`.createdBySupplier == 1 AND `TRANSACTION`.deleted == 0  THEN 1 ELSE 0 END) as newActivityCount,\n" +
                        "            MAX(`TRANSACTION`.updateTime ) as lastActivityTime,\n" +
                        "            supplier.lastViewTime,\n" +
                        "            supplier.txnAlertEnabled,\n" +
                        "            supplier.lang,\n" +
                        "            MAX(`TRANSACTION`.syncing ) as syncing,\n" +
                        "            MAX(`TRANSACTION`.lastSyncTime ) as lastSyncTime,\n" +
                        "            supplier.addTransactionRestricted,\n" +
                        "            supplier.state,\n" +
                        "            supplier.blockedBySupplier\n" +
                        "        FROM Supplier\n" +
                        "        LEFT OUTER JOIN `TRANSACTION` ON Supplier.id = `TRANSACTION`.supplierId\n" +
                        "        GROUP BY Supplier.id"
                )
            }
        }

        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `NotificationReminder` (`id` TEXT NOT NULL, `accountId` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `expiresAt` TEXT NOT NULL, `status` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        private fun migration7To8(multipleAccountsDatabaseMigrationHandler: MultipleAccountsDatabaseMigrationHandler): Migration =
            object : Migration(7, 8) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    multipleAccountsDatabaseMigrationHandler.execute(
                        database = database,
                        tableName = "`Supplier`"
                    )
                    multipleAccountsDatabaseMigrationHandler.execute(
                        database = database,
                        tableName = "`Transaction`"
                    )
                    multipleAccountsDatabaseMigrationHandler.execute(
                        database = database,
                        tableName = "`NotificationReminder`"
                    )
                    database.execSQL("DROP VIEW IF EXISTS `SupplierWithTransactionsInfo`")
                    database.execSQL(
                        "CREATE VIEW `SupplierWithTransactionsInfo` AS SELECT\n" +
                            "            supplier.id,\n" +
                            "            supplier.registered,\n" +
                            "            supplier.deleted,\n" +
                            "            supplier.createTime,\n" +
                            "            supplier.txnStartTime,\n" +
                            "            supplier.name,\n" +
                            "            supplier.mobile,\n" +
                            "            supplier.address,\n" +
                            "            supplier.profileImage,\n" +
                            "            supplier.restrictContactSync,\n" +
                            "            SUM(CASE WHEN `TRANSACTION`.deleted == 1 THEN 0 WHEN `Transaction`.transactionState == 0 THEN 0 WHEN `TRANSACTION`.payment == 0 THEN -1 * `TRANSACTION`.amount ELSE `TRANSACTION`.amount END) as balance,\n" +
                            "            SUM(CASE WHEN supplier.lastViewTime == 0 THEN 0 WHEN `TRANSACTION`.updateTime > supplier.lastViewTime AND `TRANSACTION`.createdBySupplier == 1 AND `TRANSACTION`.deleted == 0  THEN 1 ELSE 0 END) as newActivityCount,\n" +
                            "            MAX(`TRANSACTION`.updateTime ) as lastActivityTime,\n" +
                            "            supplier.lastViewTime,\n" +
                            "            supplier.txnAlertEnabled,\n" +
                            "            supplier.lang,\n" +
                            "            MAX(`TRANSACTION`.syncing ) as syncing,\n" +
                            "            MAX(`TRANSACTION`.lastSyncTime ) as lastSyncTime,\n" +
                            "            supplier.addTransactionRestricted,\n" +
                            "            supplier.state,\n" +
                            "            supplier.blockedBySupplier,\n" +
                            "            supplier.businessId as businessId\n" +
                            "        FROM Supplier\n" +
                            "        LEFT OUTER JOIN `TRANSACTION` ON Supplier.id = `TRANSACTION`.supplierId\n" +
                            "        GROUP BY Supplier.id"
                    )
                }
            }
    }
}
