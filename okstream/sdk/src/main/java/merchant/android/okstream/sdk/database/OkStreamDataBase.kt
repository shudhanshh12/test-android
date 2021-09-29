package merchant.android.okstream.sdk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    version = OkStreamDataBase.DB_VERSION,
    entities = [PublishMessage::class]
)
abstract class OkStreamDataBase : RoomDatabase() {
    abstract fun contactsDataBaseDao(): OkStreamDataBaseDao

    companion object {
        const val DB_VERSION = 1
        private const val DB_NAME = "okstream.db"

        private var INSTANCE: OkStreamDataBase? = null

        fun getInstance(context: Context): OkStreamDataBase {
            if (INSTANCE == null) {

                synchronized(OkStreamDataBase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, OkStreamDataBase::class.java, DB_NAME)
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}
