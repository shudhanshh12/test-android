package `in`.okcredit.merchant.rewards.store.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(version = RewardsDataBase.DB_VERSION, entities = [Rewards::class])
@TypeConverters(DateTimeRoomCodec::class)
abstract class RewardsDataBase : RoomDatabase() {
    abstract fun rewardsDataBaseDao(): RewardsDataBaseDao

    companion object {
        const val DB_VERSION = 4
        const val DB_NAME = "okcredit-rewards.db"

        private var INSTANCE: RewardsDataBase? = null

        fun getInstance(context: Context): RewardsDataBase {
            if (INSTANCE == null) {

                synchronized(RewardsDataBase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, RewardsDataBase::class.java, DB_NAME)
                            .addMigrations(REWARDS_MIGRATION_1_2)
                            .addMigrations(REWARDS_MIGRATION_2_3)
                            .addMigrations(REWARDS_MIGRATION_3_4)
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        private val REWARDS_MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Rewards ADD COLUMN featureName TEXT")
                database.execSQL("ALTER TABLE Rewards ADD COLUMN featureTitle TEXT")
                database.execSQL("ALTER TABLE Rewards ADD COLUMN description TEXT")
                database.execSQL("ALTER TABLE Rewards ADD COLUMN deepLink TEXT")
                database.execSQL("ALTER TABLE Rewards ADD COLUMN icon TEXT")
                database.execSQL("ALTER TABLE Rewards ADD COLUMN onHold INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val REWARDS_MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE Rewards")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS Rewards (
                    `id`            TEXT NOT NULL PRIMARY KEY,
                    `createTime`    INTEGER NOT NULL,
                    `updateTime`    INTEGER NOT NULL,
                    `status`        TEXT NOT NULL,
                    `rewardType`    TEXT,
                    `amount`        INTEGER NOT NULL,
                    `featureName`   TEXT,
                    `featureTitle`  TEXT,
                    `description`   TEXT,
                    `deepLink`      TEXT,
                    `icon`          TEXT
                    )"""
                )
            }
        }

        private val REWARDS_MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Rewards ADD COLUMN labels TEXT NOT NULL DEFAULT '{}'")
                database.execSQL("ALTER TABLE Rewards ADD COLUMN createdBy TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
