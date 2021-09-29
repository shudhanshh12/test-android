package `in`.okcredit.merchant.collection.store.database

import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    version = CollectionDataBase.DB_VERSION,
    entities = [
        Collection::class,
        CollectionProfile::class,
        CollectionShareInfo::class,
        KycExternalEntity::class,
        CollectionOnlinePaymentEntity::class,
        CustomerAdditionalInfoEntity::class,
        CustomerCollectionProfile::class,
        SupplierCollectionProfile::class,
    ]
)
@TypeConverters(DateTimeRoomCodec::class)
abstract class CollectionDataBase : RoomDatabase() {
    abstract fun collectionDataBaseDao(): CollectionDataBaseDao
    abstract fun kycRiskDao(): KycRiskDao

    companion object {
        const val DB_VERSION = 15
        const val DB_NAME = "okcredit-collection.db"

        private var INSTANCE: CollectionDataBase? = null

        fun getInstance(
            context: Context,
            multipleAccountsDatabaseMigrationHandler: MultipleAccountsDatabaseMigrationHandler,
        ): CollectionDataBase {
            if (INSTANCE == null) {

                synchronized(CollectionDataBase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context,
                            CollectionDataBase::class.java,
                            DB_NAME
                        )
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
                                migrate13To14(multipleAccountsDatabaseMigrationHandler),
                                MIGRATION_14_15,
                            )
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Collection " + " ADD COLUMN paymentOriginName TEXT")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CollectionProfile " + " ADD COLUMN merchant_vpa TEXT")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN isSupplier INTEGER default 0 not null")
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN name TEXT")
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN mobile TEXT")
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN linkVpa TEXT")
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN paymentAddress TEXT")
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN type TEXT")
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN upiVpa TEXT")
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN fromMerchantPaymentLink TEXT")
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN fromMerchantUpiIntent TEXT")
            }
        }

        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `KycExternalEntity` (`merchantId` TEXT NOT NULL, `kyc` TEXT NOT NULL, `upiDailyLimit` INTEGER NOT NULL, `nonUpiDailyLimit` INTEGER NOT NULL, `upiDailyTransactionAmount` INTEGER NOT NULL, `nonUpiDailyTransactionAmount` INTEGER NOT NULL, `category` TEXT NOT NULL, PRIMARY KEY(`merchantId`))")
            }
        }

        val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `CollectionOnlinePaymentEntity` (`id` TEXT NOT NULL, `createdTime` INTEGER NOT NULL, `updatedTime` INTEGER NOT NULL, `status` INTEGER NOT NULL, `merchantId` TEXT NOT NULL, `accountId` TEXT NOT NULL, `amount` REAL NOT NULL, `paymentId` TEXT NOT NULL, `payoutId` TEXT NOT NULL, `paymentSource` TEXT NOT NULL, `paymentMode` TEXT NOT NULL, `type` TEXT NOT NULL, `read` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
        val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN linkId TEXT")
                database.execSQL("ALTER TABLE Collection " + " ADD COLUMN paymentId TEXT")
            }
        }
        val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN googlePayEnabled INTEGER default 1 not null")
                database.execSQL("DELETE from `CollectionOnlinePaymentEntity`")
            }
        }
        val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CollectionOnlinePaymentEntity " + " ADD COLUMN errorCode TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE CollectionOnlinePaymentEntity " + " ADD COLUMN errorDescription TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE Collection " + " ADD COLUMN errorCode TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE Collection " + " ADD COLUMN errorDescription TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_10_11: Migration = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Collection " + " ADD COLUMN blindPay INTEGER NOT NULL DEFAULT 0 ")
                database.execSQL("CREATE TABLE IF NOT EXISTS `CustomerAdditionalInfoEntity` (`id` TEXT NOT NULL, `link` TEXT NOT NULL, `status` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `message` TEXT NOT NULL, `youtubeLink` TEXT NOT NULL, `customerMerchantId` TEXT NOT NULL,`ledgerSeen` INTEGER NOT NULL,  PRIMARY KEY(`id`))")
                database.execSQL("ALTER TABLE CollectionCustomerProfile " + " ADD COLUMN paymentIntent INTEGER default 0 not null")
            }
        }

        val MIGRATION_11_12: Migration = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `SupplierCollectionProfile` (`accountId` TEXT NOT NULL, `messageLink` TEXT, `linkId` TEXT, `name` TEXT, `type` TEXT, `paymentAddress` TEXT, PRIMARY KEY(`accountId`))")
                database.execSQL("CREATE TABLE IF NOT EXISTS `CustomerCollectionProfile` (`customerId` TEXT NOT NULL, `messageLink` TEXT, `message` TEXT, `qrIntent` TEXT, `showImage` INTEGER NOT NULL, `linkId` TEXT, `googlePayEnabled` INTEGER NOT NULL, `paymentIntent` INTEGER NOT NULL, PRIMARY KEY(`customerId`))")
                database.execSQL("INSERT INTO `SupplierCollectionProfile` (accountId,messageLink,linkId,name,type,paymentAddress) SELECT customer_id as accountId,message_link as messageLink,linkId,name,type,paymentAddress FROM CollectionCustomerProfile where isSupplier = 1")
                database.execSQL("INSERT INTO `CustomerCollectionProfile` (customerId,messageLink,message,qrIntent,showImage,linkId,googlePayEnabled,paymentIntent) SELECT customer_id as customerId,message_link as messageLink,message,qr_intent as qrIntent,show_image as showImage,linkId,googlePayEnabled,paymentIntent FROM CollectionCustomerProfile where isSupplier = 0")
                database.execSQL("DROP TABLE IF EXISTS `CollectionCustomerProfile`")
            }
        }

        val MIGRATION_12_13: Migration = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE SupplierCollectionProfile " + " ADD COLUMN destinationUpdateAllowed INTEGER NOT NULL DEFAULT 0 ")
                database.execSQL("ALTER TABLE CustomerCollectionProfile " + " ADD COLUMN destinationUpdateAllowed INTEGER NOT NULL DEFAULT 0 ")
                database.execSQL("ALTER TABLE CustomerCollectionProfile " + " ADD COLUMN cashbackEligible INTEGER NOT NULL DEFAULT 0 ")
                database.execSQL("ALTER TABLE Collection " + " ADD COLUMN cashbackGiven INTEGER NOT NULL DEFAULT 0 ")
                database.execSQL("DELETE from `Collection` where id = '' OR status = 0")
            }
        }

        private fun migrate13To14(migrationHandler: MultipleAccountsDatabaseMigrationHandler): Migration =
            object : Migration(13, 14) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("DELETE from `CollectionOnlinePaymentEntity`")
                    migrationHandler.execute(database, "`Collection`")
                    migrationHandler.execute(database, "`CollectionShareInfo`")
                    migrationHandler.execute(database, "`CollectionOnlinePaymentEntity`")
                    migrationHandler.execute(database, "`CustomerAdditionalInfoEntity`")
                    migrationHandler.execute(database, "`CustomerCollectionProfile`")
                    migrationHandler.execute(database, "`SupplierCollectionProfile`")
                }
            }

        val MIGRATION_14_15: Migration = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `CollectionProfile` " + " ADD COLUMN `kyc_limit` INTEGER NOT NULL DEFAULT 0 ")
                database.execSQL("ALTER TABLE `CollectionProfile` " + " ADD COLUMN `remaining_limit INTEGER` NOT NULL DEFAULT 0 ")
                database.execSQL("ALTER TABLE `CollectionProfile` " + " ADD COLUMN `limit_type` TEXT")
                database.execSQL("ALTER TABLE `CollectionProfile` " + " ADD COLUMN `merchant_qr_enabled` INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
