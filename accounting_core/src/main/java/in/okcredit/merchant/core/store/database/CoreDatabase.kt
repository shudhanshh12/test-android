package `in`.okcredit.merchant.core.store.database

import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("LongLine")
@Database(
    version = CoreDatabase.DB_VERSION,
    entities = [Transaction::class, Command::class, Customer::class, SuggestedCustomerIdsForAddTransaction::class],
    views = [CustomerWithTransactionsInfo::class]
)
@TypeConverters(TimestampMapper::class, CommandTypeMapper::class)
abstract class CoreDatabase : RoomDatabase() {
    abstract fun coreDatabaseDao(): CoreDatabaseDao

    companion object {
        const val DB_VERSION = 7
        private const val DB_NAME = "okcredit-core2.db"

        internal var INSTANCE: CoreDatabase? = null

        fun getInstance(
            context: Context,
            multipleAccountsDatabaseMigrationHandler: MultipleAccountsDatabaseMigrationHandler
        ): CoreDatabase {
            if (INSTANCE == null) {

                synchronized(CoreDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, CoreDatabase::class.java, DB_NAME)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .addMigrations(MIGRATION_4_5)
                            .addMigrations(MIGRATION_5_6)
                            .addMigrations(
                                migration6To7(
                                    multipleAccountsDatabaseMigrationHandler
                                )
                            )
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `Transaction` " + " ADD COLUMN amountUpdated INTEGER DEFAULT 0 not null")
                database.execSQL("ALTER TABLE `Transaction` " + " ADD COLUMN amountUpdatedAt INTEGER")
                database.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`")
                database.execSQL(
                    """CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT
            Customer.id,
            Customer.status,
            Customer.mobile,
            Customer.description,
            Customer.createdAt,
            agg.balance as balance,
            agg.transactionCount as transactionCount,
            agg.lastActivity as lastActivity,
            agg.lastPayment as lastPayment,
            Customer.accountUrl,
            Customer.profileImage,
            Customer.address,
            Customer.email,
            MAX(`Transaction`.billDate ) as lastBillDate,
            agg.newActivityCount as newActivityCount,
            Customer.lastViewTime,
            Customer.registered,
            Customer.txnAlertEnabled,
            Customer.lang,
            Customer.reminderMode,
            Customer.txnStartTime,
            Customer.isLiveSales,
            Customer.addTransactionRestricted,
            Customer.blockedByCustomer,
            Customer.state,
            Customer.restrictContactSync,
            MAX(Case when `Transaction`.isdeleted == 1 then (case when `Transaction`.type == 1 then 0  when `Transaction`.category == 1 then 6 else 1 end) when `Transaction`.isdeleted == 0 then (case  when `Transaction`.amountUpdated == 1 then (case  when `Transaction`.type == 1 then 8 else 9 end)  when `Transaction`.type == 1 then 2 when `Transaction`.state == 0 then 5 when `Transaction`.category == 1 then 7 else 3 end) else 4 end) as lastActivityMetaInfo,
            `Transaction`.amount as lastAmount
        FROM Customer
        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
        LEFT JOIN (
            SELECT
                    Customer.id     as id,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.state == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amount ELSE `Transaction`.amount END) as balance,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN (case when `Transaction`.amountUpdatedAt >= `Transaction`.createdAt then `Transaction`.amountUpdatedAt else `Transaction`.createdAt end) ELSE Customer.createdAt END) as lastActivity,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,
                    MAX(`Transaction`.billDate) as lastBillDate,
                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount
                FROM Customer
                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
                GROUP BY Customer.id) as agg on agg.id = Customer.id
        where (`Transaction`.deleteTime == agg.lastactivity) or (`Transaction`.createdAt == agg.lastactivity) or (customer.createdAt == agg.lastactivity) or (`Transaction`.amountUpdatedAt == agg.lastactivity)
        group by customer.id"""
                )
            }
        }

        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS SuggestedCustomerIdsForAddTransaction (`id` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`")
                database.execSQL(
                    """CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT
            Customer.id,
            Customer.status,
            Customer.mobile,
            Customer.description,
            Customer.createdAt,
            agg.balance as balance,
            agg.transactionCount as transactionCount,
            agg.lastActivity as lastActivity,
            agg.lastPayment as lastPayment,
            Customer.accountUrl,
            Customer.profileImage,
            Customer.address,
            Customer.email,
            MAX(`Transaction`.billDate ) as lastBillDate,
            agg.newActivityCount as newActivityCount,
            Customer.lastViewTime,
            Customer.registered,
            Customer.txnAlertEnabled,
            Customer.lang,
            Customer.reminderMode,
            Customer.txnStartTime,
            Customer.isLiveSales,
            Customer.addTransactionRestricted,
            Customer.blockedByCustomer,
            Customer.state,
            Customer.restrictContactSync,
            MAX(Case when `Transaction`.isdeleted == 1 then (case when `Transaction`.type == 1 then 0  when `Transaction`.category == 1 then 6 else 1 end) when `Transaction`.isdeleted == 0 then (case  when `Transaction`.amountUpdated == 1 then (case  when `Transaction`.type == 1 then 8 else 9 end)  when `Transaction`.type == 1 then 2 when `Transaction`.state == 0 then 5 when `Transaction`.category == 1 then 7 else 3 end) else 4 end) as lastActivityMetaInfo,
            `Transaction`.amount as lastAmount
        FROM Customer
        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
        LEFT JOIN (
            SELECT
                    Customer.id     as id,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.state == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amount ELSE `Transaction`.amount END) as balance,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN (case when `Transaction`.amountUpdatedAt >= `Transaction`.createdAt then `Transaction`.amountUpdatedAt else `Transaction`.createdAt end) ELSE Customer.createdAt END) as lastActivity,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,
                    MAX(`Transaction`.billDate) as lastBillDate,
                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount
                FROM Customer
                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
                GROUP BY Customer.id) as agg on agg.id = Customer.id
        where (`Transaction`.deleteTime == agg.lastactivity) or (`Transaction`.createdAt == agg.lastactivity) or (customer.createdAt == agg.lastactivity) or (`Transaction`.amountUpdatedAt == agg.lastactivity)
        group by customer.id"""
                )
            }
        }

        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""ALTER TABLE `Customer` ADD COLUMN customerSyncStatus INTEGER DEFAULT 0 not null""")
                database.execSQL("""ALTER TABLE `Command` ADD COLUMN customerId VARCHAR DEFAULT "" not null""")
                database.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`")
                database.execSQL(
                    """CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT
            Customer.id,
            Customer.customerSyncStatus,
            Customer.status,
            Customer.mobile,
            Customer.description,
            Customer.createdAt,
            agg.balance as balance,
            agg.transactionCount as transactionCount,
            agg.lastActivity as lastActivity,
            agg.lastPayment as lastPayment,
            Customer.accountUrl,
            Customer.profileImage,
            Customer.address,
            Customer.email,
            MAX(`Transaction`.billDate ) as lastBillDate,
            agg.newActivityCount as newActivityCount,
            Customer.lastViewTime,
            Customer.registered,
            Customer.txnAlertEnabled,
            Customer.lang,
            Customer.reminderMode,
            Customer.txnStartTime,
            Customer.isLiveSales,
            Customer.addTransactionRestricted,
            Customer.blockedByCustomer,
            Customer.state,
            Customer.restrictContactSync,
            MAX(Case when `Transaction`.isdeleted == 1 then (case when `Transaction`.type == 1 then 0  when `Transaction`.category == 1 then 6 else 1 end) when `Transaction`.isdeleted == 0 then (case  when `Transaction`.amountUpdated == 1 then (case  when `Transaction`.type == 1 then 8 else 9 end)  when `Transaction`.type == 1 then 2 when `Transaction`.state == 0 then 5 when `Transaction`.category == 1 then 7 else 3 end) else 4 end) as lastActivityMetaInfo,
            `Transaction`.amount as lastAmount
        FROM Customer
        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
        LEFT JOIN (
            SELECT
                    Customer.id     as id,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.state == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amount ELSE `Transaction`.amount END) as balance,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN (case when `Transaction`.amountUpdatedAt >= `Transaction`.createdAt then `Transaction`.amountUpdatedAt else `Transaction`.createdAt end) ELSE Customer.createdAt END) as lastActivity,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,
                    MAX(`Transaction`.billDate) as lastBillDate,
                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount
                FROM Customer
                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
                GROUP BY Customer.id) as agg on agg.id = Customer.id
        where (`Transaction`.deleteTime == agg.lastactivity) or (`Transaction`.createdAt == agg.lastactivity) or (customer.createdAt == agg.lastactivity) or (`Transaction`.amountUpdatedAt == agg.lastactivity)
        group by customer.id"""
                )
            }
        }

        private fun migration6To7(multipleAccountsDatabaseMigrationHandler: MultipleAccountsDatabaseMigrationHandler): Migration =
            object : Migration(6, 7) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    multipleAccountsDatabaseMigrationHandler.execute(
                        database = database,
                        tableName = "`Transaction`"
                    )
                    multipleAccountsDatabaseMigrationHandler.execute(
                        database = database,
                        tableName = "`Command`"
                    )
                    multipleAccountsDatabaseMigrationHandler.execute(
                        database = database,
                        tableName = "`Customer`"
                    )
                    multipleAccountsDatabaseMigrationHandler.execute(
                        database = database,
                        tableName = "`SuggestedCustomerIdsForAddTransaction`"
                    )
                    database.execSQL("""ALTER TABLE `Customer` ADD COLUMN lastReminderSendTime INTEGER DEFAULT 0""")
                    database.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`")
                    database.execSQL(
                        """CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT
            Customer.id,
            Customer.customerSyncStatus,
            Customer.status,
            Customer.mobile,
            Customer.description,
            Customer.createdAt,
            agg.balance as balance,
            agg.transactionCount as transactionCount,
            agg.lastActivity as lastActivity,
            agg.lastPayment as lastPayment,
            Customer.accountUrl,
            Customer.profileImage,
            Customer.address,
            Customer.email,
            MAX(`Transaction`.billDate ) as lastBillDate,
            agg.newActivityCount as newActivityCount,
            Customer.lastViewTime,
            Customer.registered,
            Customer.txnAlertEnabled,
            Customer.lang,
            Customer.reminderMode,
            Customer.txnStartTime,
            Customer.isLiveSales,
            Customer.addTransactionRestricted,
            Customer.blockedByCustomer,
            Customer.state,
            Customer.restrictContactSync,
            Customer.businessId as businessId,
            Customer.lastReminderSendTime as lastReminderSendTime,
            MAX(Case when `Transaction`.isdeleted == 1 then (case when `Transaction`.type == 1 then 0  when `Transaction`.category == 1 then 6 else 1 end) when `Transaction`.isdeleted == 0 then (case  when `Transaction`.amountUpdated == 1 then (case  when `Transaction`.type == 1 then 8 else 9 end)  when `Transaction`.type == 1 then 2 when `Transaction`.state == 0 then 5 when `Transaction`.category == 1 then 7 else 3 end) else 4 end) as lastActivityMetaInfo,
            `Transaction`.amount as lastAmount
        FROM Customer
        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
        LEFT JOIN (
            SELECT
                    Customer.id     as id,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.state == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amount ELSE `Transaction`.amount END) as balance,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN (case when `Transaction`.amountUpdatedAt >= `Transaction`.createdAt then `Transaction`.amountUpdatedAt else `Transaction`.createdAt end) ELSE Customer.createdAt END) as lastActivity,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,
                    MAX(`Transaction`.billDate) as lastBillDate,
                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount
                FROM Customer
                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
                GROUP BY Customer.id) as agg on agg.id = Customer.id
        where (`Transaction`.deleteTime == agg.lastactivity) or (`Transaction`.createdAt == agg.lastactivity) or (customer.createdAt == agg.lastactivity) or (`Transaction`.amountUpdatedAt == agg.lastactivity)
        group by customer.id"""
                    )
                    // create a temp table to migrate all data with new id
                    database.execSQL("CREATE TABLE IF NOT EXISTS `CommandTemp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,`commandId` TEXT NOT NULL, `type` INTEGER NOT NULL, `value` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `transactionId` TEXT NOT NULL, `customerId` TEXT NOT NULL, `businessId` TEXT NOT NULL)")
                    // migrate all data from actual command table to temp table
                    database.execSQL(
                        """
                        INSERT INTO `CommandTemp` (`type`,`commandId`,`value`,`timestamp`,`transactionId`,`customerId`,`businessId`)
                        SELECT `type`,`id`as commandId, `value`,`timestamp`,`transactionId`,`customerId`,`businessId`
                        FROM `Command`
                    """
                    )
                    // drop old empty table from db
                    database.execSQL("DROP TABLE IF EXISTS `Command`")
                    // rename temp table to original table
                    database.execSQL("ALTER TABLE `CommandTemp` RENAME TO `Command`")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Command_commandId` ON `Command` (`commandId`)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS `index_Command_businessId` ON `Command` (`businessId`)")
                }
            }
    }
}
