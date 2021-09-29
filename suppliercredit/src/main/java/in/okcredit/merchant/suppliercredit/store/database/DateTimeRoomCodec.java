package in.okcredit.merchant.suppliercredit.store.database;

import androidx.room.TypeConverter;
import in.okcredit.merchant.suppliercredit.utils.DateTimeMapper;
import org.joda.time.DateTime;

public final class DateTimeRoomCodec {
    @TypeConverter
    public static DateTime fromEpoch(long timestamp) {
        return DateTimeMapper.fromEpoch(timestamp);
    }

    @TypeConverter
    public static long toEpoch(DateTime dateTime) {
        return DateTimeMapper.toEpoch(dateTime);
    }

    private DateTimeRoomCodec() {}
}
