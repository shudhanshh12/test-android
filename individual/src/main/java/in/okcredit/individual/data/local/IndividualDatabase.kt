package `in`.okcredit.individual.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    version = IndividualDatabase.DB_VERSION,
    entities = [Individual::class]
)
@TypeConverters(DateTimeRoomCodec::class)
abstract class IndividualDatabase : RoomDatabase() {

    abstract fun dao(): IndividualDao

    companion object {
        const val DB_VERSION = 1
        private const val DB_NAME = "okcredit-individual.db"

        private var INSTANCE: IndividualDatabase? = null

        fun getInstance(context: Context): IndividualDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, IndividualDatabase::class.java, DB_NAME).build()
            }
            return INSTANCE!!
        }
    }
}
