package tech.okcredit.contacts.store.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.jetbrains.annotations.NonNls

@Database(
    version = ContactsDataBase.DB_VERSION,
    entities = [Contact::class]
)
abstract class ContactsDataBase : RoomDatabase() {
    abstract fun contactsDataBaseDao(): ContactsDataBaseDao

    companion object {
        const val DB_VERSION = 2
        const val DB_NAME = "okcredit-contact-v2.db"

        private var INSTANCE: ContactsDataBase? = null

        fun getInstance(context: Context): ContactsDataBase {
            if (INSTANCE == null) {

                synchronized(ContactsDataBase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, ContactsDataBase::class.java, DB_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(@NonNls database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE Contact")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS Contact (" +
                        "`mobile` TEXT NOT NULL PRIMARY KEY," +
                        "`phoneBookId` TEXT NOT NULL," +
                        "`name` TEXT, " +
                        "`picUri` TEXT," +
                        "`found` INTEGER  NOT NULL," +
                        "`timestamp` INTEGER  NOT NULL," +
                        "`synced` INTEGER  NOT NULL," +
                        "`type` INTEGER  NOT NULL" +
                        ")"
                )
            }
        }
    }
}
