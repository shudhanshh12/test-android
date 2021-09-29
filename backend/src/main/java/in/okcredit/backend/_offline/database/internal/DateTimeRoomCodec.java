package in.okcredit.backend._offline.database.internal;

import androidx.room.TypeConverter;
import org.joda.time.DateTime;
import tech.okcredit.android.base.utils.DateTimeMapper;

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
