package `in`.okcredit.merchant.rewards.store.database

import `in`.okcredit.merchant.rewards.utils.DateTimeMapper
import androidx.room.TypeConverter
import org.joda.time.DateTime

object DateTimeRoomCodec {
    @JvmStatic
    @TypeConverter
    fun fromEpoch(timestamp: Long): DateTime = DateTimeMapper.fromEpoch(timestamp)

    @JvmStatic
    @TypeConverter
    fun toEpoch(dateTime: DateTime?): Long = DateTimeMapper.toEpoch(dateTime)
}
