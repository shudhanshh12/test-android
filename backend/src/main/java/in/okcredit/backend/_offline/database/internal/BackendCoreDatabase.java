package in.okcredit.backend._offline.database.internal;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import in.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler;

@Database(
        version = BackendCoreDatabase.DB_VERSION,
        entities = {
            DbEntities.Customer.class,
            DbEntities.Transaction.class,
            DbEntities.DueInfo.class,
        },
        views = {CustomerWithTransactionsInfo.class})
@TypeConverters({DateTimeRoomCodec.class})
public abstract class BackendCoreDatabase extends RoomDatabase {
    public static final Migration MIGRATION_20_21 =
            new Migration(20, 21) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL(
                            "ALTER TABLE `Transaction` "
                                    + " ADD COLUMN amountUpdated INTEGER DEFAULT 0 not null");
                    database.execSQL(
                            "ALTER TABLE `Transaction` " + " ADD COLUMN amountUpdatedAt INTEGER");
                    database.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`");
                    database.execSQL(
                            "CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT\n"
                                    + "            Customer.id,\n"
                                    + "            Customer.status,\n"
                                    + "            Customer.mobile,\n"
                                    + "            Customer.description,\n"
                                    + "            Customer.createdAt,\n"
                                    + "            0 as balance,\n"
                                    + "            agg.balanceV2 as balanceV2,\n"
                                    + "            agg.transactionCount as transactionCount,\n"
                                    + "            agg.lastActivity as lastActivity,\n"
                                    + "            agg.lastPayment as lastPayment,\n"
                                    + "            DueInfo.is_due_active as isDueActive,\n"
                                    + "            DueInfo.active_date as activeDate,\n"
                                    + "            DueInfo.is_custom_date_set as isCustomDateSet,\n"
                                    + "            Customer.accountUrl,\n"
                                    + "            Customer.profileImage,\n"
                                    + "            Customer.address,\n"
                                    + "            Customer.email,\n"
                                    + "            MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "            agg.newActivityCount as newActivityCount,\n"
                                    + "            Customer.lastViewTime,\n"
                                    + "            Customer.registered,\n"
                                    + "            Customer.txnAlertEnabled,\n"
                                    + "            Customer.lang,\n"
                                    + "            Customer.reminderMode,\n"
                                    + "            Customer.txnStartTime,\n"
                                    + "            Customer.isLiveSales,\n"
                                    + "            Customer.addTransactionRestricted,\n"
                                    + "            Customer.blockedByCustomer,\n"
                                    + "            Customer.restrictContactSync,\n"
                                    + "            Customer.state,\n"
                                    + "            MAX(Case when `Transaction`.isdeleted ==1 then (case when `Transaction`.type==1 then 0  when `Transaction`.transactionCategory==1 then 6 else 1 end) when `Transaction`.isdeleted == 0 then (case  when `Transaction`.amountUpdated==1 then (case  when `Transaction`.type==1 then 8 else 9 end)  when `Transaction`.type==1 then 2 when `Transaction`.transactionState == 0 then 5 when `Transaction`.transactionCategory==1 then 7 else 3 end) else 4 end) as lastActivityMetaInfo,\n"
                                    + "            `Transaction`.amountV2 as lastAmount\n"
                                    + "        FROM Customer\n"
                                    + "        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "        LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "        LEFT JOIN (\n"
                                    + "            SELECT\n"
                                    + "                    Customer.id     as id,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.transactionState == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amountV2 ELSE `Transaction`.amountV2 END) as balanceV2,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN (case when `Transaction`.amountUpdatedAt >= `Transaction`.createdAt then `Transaction`.amountUpdatedAt else `Transaction`.createdAt end) ELSE Customer.createdAt END) as lastActivity,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,\n"
                                    + "                    MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount\n"
                                    + "                FROM Customer\n"
                                    + "                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "                LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "                GROUP BY Customer.id) as agg on agg.id=Customer.id\n"
                                    + "        where (`Transaction`.deleteTime==agg.lastactivity) or (`Transaction`.createdAt==agg.lastactivity) or (customer.createdAt==agg.lastactivity)  or (`Transaction`.amountUpdatedAt==agg.lastactivity) \n"
                                    + "        group by customer.id");
                }
            };
    public static final String DB_NAME = "okcredit-core.db";

    private static BackendCoreDatabase INSTANCE;
    static final int DB_VERSION = 24;

    private static final Migration MIGRATION_1_2 =
            new Migration(1, 2) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE Customer " + " ADD COLUMN profileImage TEXT");
                    database.execSQL("ALTER TABLE Customer " + " ADD COLUMN address TEXT");
                    database.execSQL("ALTER TABLE Customer " + " ADD COLUMN email TEXT");
                }
            };

    private static final Migration MIGRATION_2_3 =
            new Migration(2, 3) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE Merchant " + " ADD COLUMN profileImage TEXT");
                    database.execSQL("ALTER TABLE Merchant " + " ADD COLUMN address TEXT");
                    database.execSQL("ALTER TABLE Merchant " + " ADD COLUMN addressLatitude REAL");
                    database.execSQL("ALTER TABLE Merchant " + " ADD COLUMN addressLongitude REAL");
                }
            };

    private static final Migration MIGRATION_3_4 =
            new Migration(3, 4) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE Merchant " + " ADD COLUMN about TEXT");
                    database.execSQL("ALTER TABLE Merchant " + " ADD COLUMN email TEXT");
                    database.execSQL("ALTER TABLE Merchant " + " ADD COLUMN contactName TEXT");
                }
            };

    private static final Migration MIGRATION_4_5 =
            new Migration(4, 5) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE `Transaction` " + " ADD COLUMN billDate INTEGER");
                    database.execSQL(
                            "ALTER TABLE `Transaction` " + " ADD COLUMN updatedAt INTEGER");
                    database.execSQL(
                            "ALTER TABLE `Transaction` "
                                    + " ADD COLUMN smsSent INTEGER DEFAULT 1 not null");
                }
            };

    private static final Migration MIGRATION_5_6 =
            new Migration(5, 6) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {
                    database.execSQL(
                            "CREATE TABLE IF NOT EXISTS CustomerPreference (`customerId` TEXT NOT NULL, `key` TEXT NOT NULL, `value` TEXT NOT NULL, `isSynced` INTEGER NOT NULL, PRIMARY KEY(`customerId`, `key`))");
                }
            };

    @NonNls
    private static final Migration MIGRATION_6_7 =
            new Migration(6, 7) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {

                    // * Transaction Table Changes //
                    // 1. add column
                    database.execSQL(
                            "ALTER TABLE `Transaction` ADD COLUMN amountV2 INTEGER DEFAULT 0 not null");
                    // 2. set values
                    database.execSQL(
                            "UPDATE `Transaction` SET amountV2 = CAST(amount * 100 AS INTEGER)");

                    database.execSQL(
                            "ALTER TABLE `Transaction` " + " ADD COLUMN collectionId TEXT");

                    // * Customer Table Changes //
                    // 1. add column
                    database.execSQL(
                            "ALTER TABLE Customer "
                                    + " ADD COLUMN balanceV2 INTEGER DEFAULT 0 not null");
                    // 2. set values
                    database.execSQL(
                            "UPDATE Customer SET balanceV2 = CAST(balance * 100 AS INTEGER)");

                    // * Collection Table Added //
                    database.execSQL(
                            "CREATE TABLE IF NOT EXISTS Collection ("
                                    + "`collectionId` TEXT NOT NULL, "
                                    + "`createTime` INTEGER , "
                                    + "`updateTime` INTEGER , "
                                    + "`status` INTEGER , "
                                    + "`upiVpa` TEXT, "
                                    + "`customerId` TEXT, "
                                    + "`amountRequested` INTEGER DEFAULT 0 not null, "
                                    + "`amountCollected` INTEGER DEFAULT 0 not null, "
                                    + "`fee` INTEGER DEFAULT 0 not null, "
                                    + "`paymentLink` TEXT, "
                                    + "`seen` INTEGER , "
                                    + "`events` TEXT, "
                                    + "`expireTime` INTEGER , "
                                    + "PRIMARY KEY(`collectionId`))");

                    // * Merchant Table Changes //
                    database.execSQL("ALTER TABLE Merchant " + " ADD COLUMN upiVpa TEXT");
                }
            };

    private static final Migration MIGRATION_7_8 =
            new Migration(7, 8) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {

                    // * Collection Share Info Table Added //
                    database.execSQL(
                            "CREATE TABLE IF NOT EXISTS CollectionShareInfo ("
                                    + "`customerId` TEXT NOT NULL, "
                                    + "`sharedTime` INTEGER , "
                                    + "PRIMARY KEY(`customerId`))");

                    // * Reward Table Added //
                    database.execSQL(
                            "CREATE TABLE IF NOT EXISTS Reward ("
                                    + "`id` TEXT NOT NULL, "
                                    + "`createTime` INTEGER , "
                                    + "`updateTime` INTEGER , "
                                    + "`status` INTEGER , "
                                    + "`rewardType` TEXT, "
                                    + "`amount` INTEGER NOT NULL, "
                                    + "`claimed` INTEGER , "
                                    + "`events` TEXT, "
                                    + "PRIMARY KEY(`id`))");

                    // * Added lastBillDate in Customer Table //
                    database.execSQL(
                            "ALTER TABLE Customer " + " ADD COLUMN lastBillDate INTEGER DEFAULT 0");
                }
            };

    private static final Migration MIGRATION_8_9 =
            new Migration(8, 9) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {
                    // * Added lastBillDate in Customer Table //
                    database.execSQL(
                            "ALTER TABLE Customer ADD COLUMN newActivityCount INTEGER DEFAULT 0 not null");
                    database.execSQL(
                            "ALTER TABLE Customer ADD COLUMN lastViewTime INTEGER DEFAULT 0");
                    database.execSQL(
                            "ALTER TABLE Customer ADD COLUMN registered INTEGER DEFAULT 0 not null");
                    database.execSQL(
                            "ALTER TABLE Customer ADD COLUMN txnAlertEnabled INTEGER DEFAULT 1 not null");
                    database.execSQL("ALTER TABLE Customer ADD COLUMN lang TEXT");
                    database.execSQL("ALTER TABLE Customer ADD COLUMN reminderMode TEXT");

                    database.execSQL(
                            "ALTER TABLE `Transaction` "
                                    + " ADD COLUMN createdByCustomer INTEGER DEFAULT 0 not null");
                    database.execSQL(
                            "ALTER TABLE `Transaction` "
                                    + " ADD COLUMN deletedByCustomer INTEGER DEFAULT 0 not null");
                }
            };

    private static final Migration MIGRATION_9_10 =
            new Migration(9, 10) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {
                    // * Added txnStartTime in Customer Table //
                    database.execSQL(
                            "ALTER TABLE Customer ADD COLUMN txnStartTime INTEGER DEFAULT 0");
                }
            };

    private static final Migration MIGRATION_10_11 =
            new Migration(10, 11) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {
                    // * Dropped Collection and Rewards Tables //
                    database.execSQL("DROP TABLE Collection");
                    database.execSQL("DROP TABLE Reward");
                    database.execSQL("DROP TABLE CollectionShareInfo");
                }
            };

    private static final Migration MIGRATION_11_12 =
            new Migration(11, 12) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {
                    // * Dropped MerchantPreference //
                    database.execSQL("DROP TABLE MerchantPreference");
                }
            };

    private static final Migration MIGRATION_12_13 =
            new Migration(12, 13) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {

                    // * Added isLiveSales//
                    database.execSQL(
                            "ALTER TABLE Customer ADD COLUMN isLiveSales INTEGER DEFAULT 0 not null");

                    // * Customer DatabaseView //
                    database.execSQL(
                            ""
                                    + "CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT\n"
                                    + "            Customer.id,\n"
                                    + "            Customer.status,\n"
                                    + "            Customer.mobile,\n"
                                    + "            Customer.description,\n"
                                    + "            Customer.createdAt,\n"
                                    + "            0 as balance,\n"
                                    + "            SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amountV2 ELSE `Transaction`.amountV2 END) as balanceV2,\n"
                                    + "            SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,\n"
                                    + "            MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastActivity,\n"
                                    + "            MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,\n"
                                    + "            Customer.accountUrl,\n"
                                    + "            Customer.profileImage,\n"
                                    + "            Customer.address,\n"
                                    + "            Customer.email,\n"
                                    + "            MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "            SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1 AND `Transaction`.isDeleted == 0  THEN 1 ELSE 0 END) as newActivityCount,\n"
                                    + "            Customer.lastViewTime,\n"
                                    + "            Customer.registered,\n"
                                    + "            Customer.txnAlertEnabled,\n"
                                    + "            Customer.lang,\n"
                                    + "            Customer.reminderMode,\n"
                                    + "            Customer.txnStartTime,\n"
                                    + "            Customer.isLiveSales\n"
                                    + "        FROM Customer\n"
                                    + "        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "        GROUP BY Customer.id");

                    // * Dropped MerchantPreference //
                    database.execSQL("DROP TABLE CustomerSync");
                }
            };

    private static final Migration MIGRATION_13_14 =
            new Migration(13, 14) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {

                    database.execSQL(
                            "CREATE TABLE IF NOT EXISTS `DueInfo` (`customerId` TEXT NOT NULL, `is_due_active` INTEGER NOT NULL, `active_date` INTEGER, `is_custom_date_set` INTEGER NOT NULL, PRIMARY KEY(`customerId`))");

                    database.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`");

                    database.execSQL(
                            "CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT\n"
                                    + "            Customer.id,\n"
                                    + "            Customer.status,\n"
                                    + "            Customer.mobile,\n"
                                    + "            Customer.description,\n"
                                    + "            Customer.createdAt,\n"
                                    + "            0 as balance,\n"
                                    + "            agg.balanceV2 as balanceV2,\n"
                                    + "            agg.transactionCount as transactionCount,\n"
                                    + "            agg.lastActivity as lastActivity,\n"
                                    + "            agg.lastPayment as lastPayment,\n"
                                    + "            DueInfo.is_due_active as isDueActive,\n"
                                    + "            DueInfo.active_date as activeDate,\n"
                                    + "            DueInfo.is_custom_date_set as isCustomDateSet,\n"
                                    + "            Customer.accountUrl,\n"
                                    + "            Customer.profileImage,\n"
                                    + "            Customer.address,\n"
                                    + "            Customer.email,\n"
                                    + "            MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "            agg.newActivityCount as newActivityCount,\n"
                                    + "            Customer.lastViewTime,\n"
                                    + "            Customer.registered,\n"
                                    + "            Customer.txnAlertEnabled,\n"
                                    + "            Customer.lang,\n"
                                    + "            Customer.reminderMode,\n"
                                    + "            Customer.txnStartTime,\n"
                                    + "            Customer.isLiveSales,\n"
                                    + "            MAX(Case when `Transaction`.isdeleted ==1 then (case when `Transaction`.type==1 then 0 else 1 end) when `Transaction`.isdeleted ==0 then (case when `Transaction`.type==1 then 2 else 3 end) else 4 end) as lastActivityMetaInfo,\n"
                                    + "            `Transaction`.amountV2 as lastAmount\n"
                                    + "        FROM Customer\n"
                                    + "        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "        LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "        LEFT JOIN (\n"
                                    + "            SELECT\n"
                                    + "                    Customer.id     as id,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amountV2 ELSE `Transaction`.amountV2 END) as balanceV2,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN `Transaction`.createdAt ELSE Customer.createdAt END) as lastActivity,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,\n"
                                    + "                    MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount\n"
                                    + "                FROM Customer\n"
                                    + "                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "                LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "                GROUP BY Customer.id) as agg on agg.id=Customer.id\n"
                                    + "        where (`Transaction`.deleteTime==agg.lastactivity) or (`Transaction`.createdAt==agg.lastactivity) or (customer.createdAt==agg.lastactivity) \n"
                                    + "        group by customer.id");
                }
            };

    private static final Migration MIGRATION_14_15 =
            new Migration(14, 15) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE `Transaction` " + " ADD COLUMN inputType TEXT");
                    database.execSQL("ALTER TABLE `Transaction` " + " ADD COLUMN voiceId TEXT");
                }
            };

    private static final Migration MIGRATION_15_16 =
            new Migration(15, 16) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase _db) {

                    _db.execSQL(
                            "ALTER TABLE Customer ADD COLUMN addTransactionRestricted INTEGER DEFAULT 0 not null");

                    _db.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`");
                    _db.execSQL(
                            "CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT\n"
                                    + "            Customer.id,\n"
                                    + "            Customer.status,\n"
                                    + "            Customer.mobile,\n"
                                    + "            Customer.description,\n"
                                    + "            Customer.createdAt,\n"
                                    + "            0 as balance,\n"
                                    + "            agg.balanceV2 as balanceV2,\n"
                                    + "            agg.transactionCount as transactionCount,\n"
                                    + "            agg.lastActivity as lastActivity,\n"
                                    + "            agg.lastPayment as lastPayment,\n"
                                    + "            DueInfo.is_due_active as isDueActive,\n"
                                    + "            DueInfo.active_date as activeDate,\n"
                                    + "            DueInfo.is_custom_date_set as isCustomDateSet,\n"
                                    + "            Customer.accountUrl,\n"
                                    + "            Customer.profileImage,\n"
                                    + "            Customer.address,\n"
                                    + "            Customer.email,\n"
                                    + "            MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "            agg.newActivityCount as newActivityCount,\n"
                                    + "            Customer.lastViewTime,\n"
                                    + "            Customer.registered,\n"
                                    + "            Customer.txnAlertEnabled,\n"
                                    + "            Customer.lang,\n"
                                    + "            Customer.reminderMode,\n"
                                    + "            Customer.txnStartTime,\n"
                                    + "            Customer.isLiveSales,\n"
                                    + "            Customer.addTransactionRestricted,\n"
                                    + "            MAX(Case when `Transaction`.isdeleted ==1 then (case when `Transaction`.type==1 then 0 else 1 end) when `Transaction`.isdeleted ==0 then (case when `Transaction`.type==1 then 2 else 3 end) else 4 end) as lastActivityMetaInfo,\n"
                                    + "            `Transaction`.amountV2 as lastAmount\n"
                                    + "        FROM Customer\n"
                                    + "        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "        LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "        LEFT JOIN (\n"
                                    + "            SELECT\n"
                                    + "                    Customer.id     as id,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amountV2 ELSE `Transaction`.amountV2 END) as balanceV2,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN `Transaction`.createdAt ELSE Customer.createdAt END) as lastActivity,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,\n"
                                    + "                    MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount\n"
                                    + "                FROM Customer\n"
                                    + "                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "                LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "                GROUP BY Customer.id) as agg on agg.id=Customer.id\n"
                                    + "        where (`Transaction`.deleteTime==agg.lastactivity) or (`Transaction`.createdAt==agg.lastactivity) or (customer.createdAt==agg.lastactivity) \n"
                                    + "        group by customer.id");
                }
            };

    private static final Migration MIGRATION_16_17 =
            new Migration(16, 17) {
                @Override
                public void migrate(@NonNls SupportSQLiteDatabase _db) {
                    _db.execSQL(
                            "ALTER TABLE `Transaction` ADD COLUMN transactionState INTEGER DEFAULT -1 NOT NULL");
                    _db.execSQL(
                            "ALTER TABLE Customer ADD COLUMN blockedByCustomer INTEGER DEFAULT 0 not null");
                    _db.execSQL("ALTER TABLE Customer ADD COLUMN state INTEGER DEFAULT 1 not null");

                    _db.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`");
                    _db.execSQL(
                            "CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT\n"
                                    + "            Customer.id,\n"
                                    + "            Customer.status,\n"
                                    + "            Customer.mobile,\n"
                                    + "            Customer.description,\n"
                                    + "            Customer.createdAt,\n"
                                    + "            0 as balance,\n"
                                    + "            agg.balanceV2 as balanceV2,\n"
                                    + "            agg.transactionCount as transactionCount,\n"
                                    + "            agg.lastActivity as lastActivity,\n"
                                    + "            agg.lastPayment as lastPayment,\n"
                                    + "            DueInfo.is_due_active as isDueActive,\n"
                                    + "            DueInfo.active_date as activeDate,\n"
                                    + "            DueInfo.is_custom_date_set as isCustomDateSet,\n"
                                    + "            Customer.accountUrl,\n"
                                    + "            Customer.profileImage,\n"
                                    + "            Customer.address,\n"
                                    + "            Customer.email,\n"
                                    + "            MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "            agg.newActivityCount as newActivityCount,\n"
                                    + "            Customer.lastViewTime,\n"
                                    + "            Customer.registered,\n"
                                    + "            Customer.txnAlertEnabled,\n"
                                    + "            Customer.lang,\n"
                                    + "            Customer.reminderMode,\n"
                                    + "            Customer.txnStartTime,\n"
                                    + "            Customer.isLiveSales,\n"
                                    + "            Customer.addTransactionRestricted,\n"
                                    + "            Customer.blockedByCustomer,\n"
                                    + "            Customer.state,\n"
                                    + "            MAX(Case when `Transaction`.isdeleted ==1 then (case when `Transaction`.type==1 then 0 else 1 end) when `Transaction`.isdeleted ==0 then (case when `Transaction`.type==1 then 2 when `Transaction`.transactionState == 0 then 5 else 3 end) else 4 end) as lastActivityMetaInfo,\n"
                                    + "            `Transaction`.amountV2 as lastAmount\n"
                                    + "        FROM Customer\n"
                                    + "        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "        LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "        LEFT JOIN (\n"
                                    + "            SELECT\n"
                                    + "                    Customer.id     as id,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.transactionState == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amountV2 ELSE `Transaction`.amountV2 END) as balanceV2,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN `Transaction`.createdAt ELSE Customer.createdAt END) as lastActivity,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,\n"
                                    + "                    MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount\n"
                                    + "                FROM Customer\n"
                                    + "                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "                LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "                GROUP BY Customer.id) as agg on agg.id=Customer.id\n"
                                    + "        where (`Transaction`.deleteTime==agg.lastactivity) or (`Transaction`.createdAt==agg.lastactivity) or (customer.createdAt==agg.lastactivity) \n"
                                    + "        group by customer.id");
                }
            };

    public static final Migration MIGRATION_17_18 =
            new Migration(17, 18) {
                @Override
                public void migrate(SupportSQLiteDatabase _db) {
                    _db.execSQL(
                            "ALTER TABLE `Transaction` ADD COLUMN transactionCategory INTEGER DEFAULT 0 NOT NULL");
                    _db.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`");
                    _db.execSQL(
                            "CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT\n"
                                    + "            Customer.id,\n"
                                    + "            Customer.status,\n"
                                    + "            Customer.mobile,\n"
                                    + "            Customer.description,\n"
                                    + "            Customer.createdAt,\n"
                                    + "            0 as balance,\n"
                                    + "            agg.balanceV2 as balanceV2,\n"
                                    + "            agg.transactionCount as transactionCount,\n"
                                    + "            agg.lastActivity as lastActivity,\n"
                                    + "            agg.lastPayment as lastPayment,\n"
                                    + "            DueInfo.is_due_active as isDueActive,\n"
                                    + "            DueInfo.active_date as activeDate,\n"
                                    + "            DueInfo.is_custom_date_set as isCustomDateSet,\n"
                                    + "            Customer.accountUrl,\n"
                                    + "            Customer.profileImage,\n"
                                    + "            Customer.address,\n"
                                    + "            Customer.email,\n"
                                    + "            MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "            agg.newActivityCount as newActivityCount,\n"
                                    + "            Customer.lastViewTime,\n"
                                    + "            Customer.registered,\n"
                                    + "            Customer.txnAlertEnabled,\n"
                                    + "            Customer.lang,\n"
                                    + "            Customer.reminderMode,\n"
                                    + "            Customer.txnStartTime,\n"
                                    + "            Customer.isLiveSales,\n"
                                    + "            Customer.addTransactionRestricted,\n"
                                    + "            Customer.blockedByCustomer,\n"
                                    + "            Customer.state,\n"
                                    + "            MAX(Case when `Transaction`.isdeleted ==1 then (case when `Transaction`.type==1 then 0  when `Transaction`.transactionCategory==1 then 6 else 1 end) when `Transaction`.isdeleted ==0 then (case when `Transaction`.type==1 then 2 when `Transaction`.transactionState == 0 then 5 when `Transaction`.transactionCategory==1 then 7 else 3 end) else 4 end) as lastActivityMetaInfo,\n"
                                    + "            `Transaction`.amountV2 as lastAmount\n"
                                    + "        FROM Customer\n"
                                    + "        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "        LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "        LEFT JOIN (\n"
                                    + "            SELECT\n"
                                    + "                    Customer.id     as id,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.transactionState == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amountV2 ELSE `Transaction`.amountV2 END) as balanceV2,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN `Transaction`.createdAt ELSE Customer.createdAt END) as lastActivity,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,\n"
                                    + "                    MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount\n"
                                    + "                FROM Customer\n"
                                    + "                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "                LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "                GROUP BY Customer.id) as agg on agg.id=Customer.id\n"
                                    + "        where (`Transaction`.deleteTime==agg.lastactivity) or (`Transaction`.createdAt==agg.lastactivity) or (customer.createdAt==agg.lastactivity) \n"
                                    + "        group by customer.id");
                }
            };

    public static final Migration MIGRATION_18_19 =
            new Migration(18, 19) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {
                    database.execSQL("DROP TABLE Merchant");
                }
            };

    public static final Migration MIGRATION_19_20 =
            new Migration(19, 20) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {
                    database.execSQL(
                            "ALTER TABLE Customer ADD COLUMN restrictContactSync INTEGER DEFAULT 0 not null");
                    database.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`");
                    database.execSQL(
                            "CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT\n"
                                    + "            Customer.id,\n"
                                    + "            Customer.status,\n"
                                    + "            Customer.mobile,\n"
                                    + "            Customer.description,\n"
                                    + "            Customer.createdAt,\n"
                                    + "            0 as balance,\n"
                                    + "            agg.balanceV2 as balanceV2,\n"
                                    + "            agg.transactionCount as transactionCount,\n"
                                    + "            agg.lastActivity as lastActivity,\n"
                                    + "            agg.lastPayment as lastPayment,\n"
                                    + "            DueInfo.is_due_active as isDueActive,\n"
                                    + "            DueInfo.active_date as activeDate,\n"
                                    + "            DueInfo.is_custom_date_set as isCustomDateSet,\n"
                                    + "            Customer.accountUrl,\n"
                                    + "            Customer.profileImage,\n"
                                    + "            Customer.address,\n"
                                    + "            Customer.email,\n"
                                    + "            MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "            agg.newActivityCount as newActivityCount,\n"
                                    + "            Customer.lastViewTime,\n"
                                    + "            Customer.registered,\n"
                                    + "            Customer.txnAlertEnabled,\n"
                                    + "            Customer.lang,\n"
                                    + "            Customer.reminderMode,\n"
                                    + "            Customer.txnStartTime,\n"
                                    + "            Customer.isLiveSales,\n"
                                    + "            Customer.addTransactionRestricted,\n"
                                    + "            Customer.blockedByCustomer,\n"
                                    + "            Customer.restrictContactSync,\n"
                                    + "            Customer.state,\n"
                                    + "            MAX(Case when `Transaction`.isdeleted ==1 then (case when `Transaction`.type==1 then 0  when `Transaction`.transactionCategory==1 then 6 else 1 end) when `Transaction`.isdeleted ==0 then (case when `Transaction`.type==1 then 2 when `Transaction`.transactionState == 0 then 5 when `Transaction`.transactionCategory==1 then 7 else 3 end) else 4 end) as lastActivityMetaInfo,\n"
                                    + "            `Transaction`.amountV2 as lastAmount\n"
                                    + "        FROM Customer\n"
                                    + "        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "        LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "        LEFT JOIN (\n"
                                    + "            SELECT\n"
                                    + "                    Customer.id     as id,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.transactionState == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amountV2 ELSE `Transaction`.amountV2 END) as balanceV2,\n"
                                    + "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN `Transaction`.createdAt ELSE Customer.createdAt END) as lastActivity,\n"
                                    + "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,\n"
                                    + "                    MAX(`Transaction`.billDate ) as lastBillDate,\n"
                                    + "                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount\n"
                                    + "                FROM Customer\n"
                                    + "                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n"
                                    + "                LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n"
                                    + "                GROUP BY Customer.id) as agg on agg.id=Customer.id\n"
                                    + "        where (`Transaction`.deleteTime==agg.lastactivity) or (`Transaction`.createdAt==agg.lastactivity) or (customer.createdAt==agg.lastactivity) \n"
                                    + "        group by customer.id");
                }
            };

    public static final Migration MIGRATION_21_22 =
            new Migration(21, 22) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {
                    database.execSQL(
                            "ALTER TABLE DueInfo ADD COLUMN is_auto_generated INTEGER DEFAULT 0 not null");
                }
            };

    public static final Migration MIGRATION_22_23 =
            new Migration(22, 23) {
                @Override
                public void migrate(@NotNull SupportSQLiteDatabase database) {
                    database.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`");
                    database.execSQL(
                            "CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT\n" +
                                    "            Customer.id,\n" +
                                    "            Customer.status,\n" +
                                    "            Customer.mobile,\n" +
                                    "            Customer.description,\n" +
                                    "            Customer.createdAt,\n" +
                                    "            0 as balance,\n" +
                                    "            agg.balanceV2 as balanceV2,\n" +
                                    "            agg.transactionCount as transactionCount,\n" +
                                    "            agg.lastActivity as lastActivity,\n" +
                                    "            agg.lastPayment as lastPayment,\n" +
                                    "            DueInfo.is_due_active as isDueActive,\n" +
                                    "            DueInfo.active_date as activeDate,\n" +
                                    "            DueInfo.is_custom_date_set as isCustomDateSet,\n" +
                                    "            Customer.accountUrl,\n" +
                                    "            Customer.profileImage,\n" +
                                    "            Customer.address,\n" +
                                    "            Customer.email,\n" +
                                    "            MAX(`Transaction`.billDate ) as lastBillDate,\n" +
                                    "            agg.newActivityCount as newActivityCount,\n" +
                                    "            Customer.lastViewTime,\n" +
                                    "            Customer.registered,\n" +
                                    "            Customer.txnAlertEnabled,\n" +
                                    "            Customer.lang,\n" +
                                    "            Customer.reminderMode,\n" +
                                    "            Customer.txnStartTime,\n" +
                                    "            Customer.isLiveSales,\n" +
                                    "            Customer.addTransactionRestricted,\n" +
                                    "            Customer.blockedByCustomer,\n" +
                                    "            Customer.restrictContactSync,\n" +
                                    "            Customer.state,\n" +
                                    "            MAX(Case when `Transaction`.isdeleted ==1 then (case when `Transaction`.type==1 then 0  when `Transaction`.transactionCategory==1 then 6 else 1 end) when `Transaction`.isdeleted == 0 then (case  when `Transaction`.amountUpdated==1 then (case  when `Transaction`.type==1 then 8 else 9 end)  when `Transaction`.type==1 then 2 when `Transaction`.transactionState == 0 then 5 when `Transaction`.transactionCategory==1 then 7 else 3 end) else 4 end) as lastActivityMetaInfo,\n" +
                                    "            `Transaction`.amountV2 as lastAmount\n" +
                                    "        FROM Customer\n" +
                                    "        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n" +
                                    "        LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n" +
                                    "        LEFT JOIN (\n" +
                                    "            SELECT\n" +
                                    "                    Customer.id     as id,\n" +
                                    "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.transactionState == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amountV2 ELSE `Transaction`.amountV2 END) as balanceV2,\n" +
                                    "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,\n" +
                                    "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN (case when `Transaction`.amountUpdatedAt >= `Transaction`.createdAt then `Transaction`.amountUpdatedAt else `Transaction`.createdAt end) ELSE Customer.createdAt END) as lastActivity,\n" +
                                    "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,\n" +
                                    "                    MAX(`Transaction`.billDate ) as lastBillDate,\n" +
                                    "                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount\n" +
                                    "                FROM Customer\n" +
                                    "                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n" +
                                    "                LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n" +
                                    "                GROUP BY Customer.id) as agg on agg.id=Customer.id\n" +
                                    "        where (`Transaction`.deleteTime==agg.lastactivity) or (`Transaction`.createdAt==agg.lastactivity) or (customer.createdAt==agg.lastactivity)  or (`Transaction`.amountUpdatedAt==agg.lastactivity)\n" +
                                    "        group by customer.id");
                }
            };

    private static Migration migrate23To24(MultipleAccountsDatabaseMigrationHandler multipleAccountsDatabaseMigrationHandler){
        return new Migration(23, 24) {
            @Override
            public void migrate(@NonNls SupportSQLiteDatabase database) {
                multipleAccountsDatabaseMigrationHandler.execute(database, "`Customer`");
                multipleAccountsDatabaseMigrationHandler.execute(database, "`Transaction`");
                multipleAccountsDatabaseMigrationHandler.execute(database, "`DueInfo`");
                database.execSQL("ALTER TABLE Customer ADD COLUMN lastReminderSendTime INTEGER DEFAULT 0");
                database.execSQL("DROP VIEW IF EXISTS `CustomerWithTransactionsInfo`");
                database.execSQL(
                        "CREATE VIEW `CustomerWithTransactionsInfo` AS SELECT\n" +
                                "            Customer.id,\n" +
                                "            Customer.status,\n" +
                                "            Customer.mobile,\n" +
                                "            Customer.description,\n" +
                                "            Customer.createdAt,\n" +
                                "            0 as balance,\n" +
                                "            agg.balanceV2 as balanceV2,\n" +
                                "            agg.transactionCount as transactionCount,\n" +
                                "            agg.lastActivity as lastActivity,\n" +
                                "            agg.lastPayment as lastPayment,\n" +
                                "            DueInfo.is_due_active as isDueActive,\n" +
                                "            DueInfo.active_date as activeDate,\n" +
                                "            DueInfo.is_custom_date_set as isCustomDateSet,\n" +
                                "            Customer.accountUrl,\n" +
                                "            Customer.profileImage,\n" +
                                "            Customer.address,\n" +
                                "            Customer.email,\n" +
                                "            MAX(`Transaction`.billDate ) as lastBillDate,\n" +
                                "            agg.newActivityCount as newActivityCount,\n" +
                                "            Customer.lastViewTime,\n" +
                                "            Customer.registered,\n" +
                                "            Customer.txnAlertEnabled,\n" +
                                "            Customer.lang,\n" +
                                "            Customer.reminderMode,\n" +
                                "            Customer.txnStartTime,\n" +
                                "            Customer.isLiveSales,\n" +
                                "            Customer.addTransactionRestricted,\n" +
                                "            Customer.blockedByCustomer,\n" +
                                "            Customer.restrictContactSync,\n" +
                                "            Customer.state,\n" +
                                "            Customer.businessId as businessId,\n" +
                                "            Customer.lastReminderSendTime,\n" +
                                "            MAX(Case when `Transaction`.isdeleted ==1 then (case when `Transaction`.type==1 then 0  when `Transaction`.transactionCategory==1 then 6 else 1 end) when `Transaction`.isdeleted == 0 then (case  when `Transaction`.amountUpdated==1 then (case  when `Transaction`.type==1 then 8 else 9 end)  when `Transaction`.type==1 then 2 when `Transaction`.transactionState == 0 then 5 when `Transaction`.transactionCategory==1 then 7 else 3 end) else 4 end) as lastActivityMetaInfo,\n" +
                                "            `Transaction`.amountV2 as lastAmount\n" +
                                "        FROM Customer\n" +
                                "        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n" +
                                "        LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n" +
                                "        LEFT JOIN (\n" +
                                "            SELECT\n" +
                                "                    Customer.id     as id,\n" +
                                "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.transactionState == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amountV2 ELSE `Transaction`.amountV2 END) as balanceV2,\n" +
                                "                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,\n" +
                                "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN (case when `Transaction`.amountUpdatedAt >= `Transaction`.createdAt then `Transaction`.amountUpdatedAt else `Transaction`.createdAt end) ELSE Customer.createdAt END) as lastActivity,\n" +
                                "                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,\n" +
                                "                    MAX(`Transaction`.billDate ) as lastBillDate,\n" +
                                "                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount\n" +
                                "                FROM Customer\n" +
                                "                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId\n" +
                                "                LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId\n" +
                                "                GROUP BY Customer.id) as agg on agg.id=Customer.id\n" +
                                "        where (`Transaction`.deleteTime==agg.lastactivity) or (`Transaction`.createdAt==agg.lastactivity) or (customer.createdAt==agg.lastactivity)  or (`Transaction`.amountUpdatedAt==agg.lastactivity)\n" +
                                "        group by customer.id");
            }
        };
    }

    public static BackendCoreDatabase getInstance(Context context, MultipleAccountsDatabaseMigrationHandler multipleAccountsDatabaseMigrationHandler) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context, BackendCoreDatabase.class, DB_NAME)
                            .addMigrations(
                                    MIGRATION_1_2,
                                    MIGRATION_2_3,
                                    MIGRATION_3_4,
                                    MIGRATION_4_5,
                                    MIGRATION_5_6,
                                    MIGRATION_6_7,
                                    MIGRATION_7_8,
                                    MIGRATION_8_9,
                                    MIGRATION_9_10,
                                    MIGRATION_10_11,
                                    MIGRATION_11_12,
                                    MIGRATION_12_13,
                                    MIGRATION_13_14,
                                    MIGRATION_14_15,
                                    MIGRATION_15_16,
                                    MIGRATION_16_17,
                                    MIGRATION_17_18,
                                    MIGRATION_18_19,
                                    MIGRATION_19_20,
                                    MIGRATION_20_21,
                                    MIGRATION_21_22,
                                    MIGRATION_22_23,
                                    migrate23To24(multipleAccountsDatabaseMigrationHandler)
                            )
                            .build();
        }
        return INSTANCE;
    }

    public abstract CustomerDao customerDao();

    public abstract TransactionDao transactionDao();

    public abstract DueInfoDao dueInfoDao();
}
