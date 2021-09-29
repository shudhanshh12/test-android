package tech.okcredit.android.base.utils;

import androidx.room.TypeConverter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public final class DateTimeMapper {
    @TypeConverter
    public static DateTime fromEpoch(long timestamp) {
        long epochMillis = timestamp * 1000L;
        if (timestamp == 0) return null;
        else return new DateTime(epochMillis, DateTimeZone.getDefault());
    }

    @TypeConverter
    public static long toEpoch(DateTime dateTime) {
        if (dateTime == null) return 0;
        else return (dateTime.getMillis() / 1000L);
    }

    private DateTimeMapper() {}
}
