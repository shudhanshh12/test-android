package `in`.okcredit.shared.service.keyval

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 1, entities = [Entry::class])
abstract class KeyValDatabase : RoomDatabase() {

    abstract fun storageDao(): KeyValDao

    companion object {

        private const val NAME = "okcredit.db"

        fun newInstance(context: Context): KeyValDatabase {
            return Room.databaseBuilder(context, KeyValDatabase::class.java, NAME).build()
        }
    }
}
