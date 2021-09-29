package `in`.okcredit.shared.store.database

import `in`.okcredit.shared.utils.Timestamp
import androidx.room.TypeConverter

class TimestampConverter {
    @TypeConverter
    fun longToTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it) }
    }

    @TypeConverter
    fun timestampToLong(timestamp: Timestamp?): Long? {
        return timestamp?.epoch
    }
}
