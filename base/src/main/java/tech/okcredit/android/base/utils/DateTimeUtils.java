package tech.okcredit.android.base.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.instacart.library.truetime.TrueTime;
import com.instacart.library.truetime.TrueTimeRx;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import tech.okcredit.android.base.R;
import tech.okcredit.android.base.language.LocaleManager;

public final class DateTimeUtils {

    private DateTimeUtils() {}

    private static final int HOURS_IN_DAY = 24;

    public static String format(DateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        DateTime today = currentDateTime().withTimeAtStartOfDay();
        DateTime day = dateTime.withTimeAtStartOfDay();

        if (day.equals(today)) {
            return dateTime.toString("hh:mm aa", getEnglishLocale());
        }

        if (today.getYear() == dateTime.getYear()) {
            return dateTime.toString("dd MMM, hh:mm aa", getEnglishLocale());
        }
        return formatLong(dateTime);
    }

    public static String formatAccountStatement(@NonNull Context context, DateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        DateTime today = currentDateTime().withTimeAtStartOfDay();
        DateTime day = dateTime.withTimeAtStartOfDay();

        if (day.equals(today)) {
            return context.getString(R.string.today)
                    + " "
                    + dateTime.toString("hh:mm aa", getEnglishLocale());
        }

        if (today.getYear() == dateTime.getYear()) {
            return dateTime.toString("dd MMM yyyy, hh:mm aa", getEnglishLocale());
        }
        return formatLong(dateTime);
    }

    public static String formatTx(@Nullable DateTime dateTime, @NonNull Context context) {
        if (dateTime == null) {
            return "";
        } else if (LocalDate.now().compareTo(new LocalDate(dateTime)) == 0) {
            return context.getString(R.string.today);
        } else if (LocalDate.now().minusDays(1).compareTo(new LocalDate(dateTime)) == 0) {
            return context.getString(R.string.yesterday);
        } else {
            return dateTime.toString("dd MMM yyyy", getEnglishLocale());
        }
    }

    public static String formatTimeOnly(DateTime dateTime) {
        if (dateTime == null) {
            return "";
        } else {
            return dateTime.toString("hh:mm aa", getEnglishLocale());
        }
    }

    public static String formatDateOnly(DateTime dateTime) {
        return dateTime.toString("dd MMM yyyy", getEnglishLocale());
    }

    public static String formatLong(DateTime dateTime) {
        return dateTime.toString("dd MMM yyyy, hh:mm aa", getEnglishLocale());
    }

    private static Locale getEnglishLocale() {
        return new Locale("en", "IN");
    }

    public static DateTime currentDateTime() {
        DateTime now;
        try {
            if (TrueTime.isInitialized()) {
                now = new DateTime(TrueTimeRx.now());
            } else {
                now = DateTime.now();
            }
        } catch (Exception e) {
            now = DateTime.now();
        }
        return now;
    }

    public static boolean isPresentDate(DateTime time) {
        DateTime dateTime = new DateTime();
        return time.withTimeAtStartOfDay().getMillis()
                == dateTime.withTimeAtStartOfDay().getMillis();
    }

    // outputs in 10 Jul
    @Nullable
    public static String getFormat1(DateTime time) {
        SimpleDateFormat simple = new SimpleDateFormat("dd MMM");
        Date result = new Date(time.getMillis());
        return simple.format(result);
    }

    // outputs in 10 Jul, 2020
    @Nullable
    public static String getFormat2(Context context, DateTime time) {
        String language = LocaleManager.getLanguage(context);
        if (language == null) {
            language = LocaleManager.LANGUAGE_ENGLISH;
        }
        SimpleDateFormat simple = new SimpleDateFormat("dd MMM, yyyy", new Locale(language));
        Date result = new Date(time.getMillis());
        return simple.format(result);
    }

    public static String getFormat4(DateTime time) {
        SimpleDateFormat simple = new SimpleDateFormat("MMM yyyy");
        Date result = new Date(time.getMillis());
        return simple.format(result);
    }

    // outputs in 10/07/2020
    @Nullable
    public static String getFormat5(DateTime time) {
        SimpleDateFormat simple = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        return simple.format(new Date(time.getMillis()));
    }

    public static boolean isDatePassed(@Nullable DateTime dateTime) {
        DateTime present = new DateTime();
        return present.withTimeAtStartOfDay().compareTo(dateTime.withTimeAtStartOfDay()) > 0;
    }

    public static boolean isFutureDate(DateTime dateTime) {
        DateTime present = new DateTime();
        return present.withTimeAtStartOfDay().compareTo(dateTime.withTimeAtStartOfDay()) < 0;
    }

    public static int getDateFromMillis(DateTime dateTime) {
        long milliSeconds = dateTime.getMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return calendar.get(Calendar.DATE);
    }

    public static int getDateFromMillis(Long timeInMillis) {

        if (timeInMillis != null) {
            return getDateFromMillis(new DateTime(timeInMillis));
        }
        return Calendar.getInstance().get(Calendar.DATE);
    }

    public static boolean isCurrentOrPassedDate(long timeInMillis) {
        DateTime present = new DateTime();
        DateTime in = new DateTime(timeInMillis);
        return present.withTimeAtStartOfDay().compareTo(in.withTimeAtStartOfDay()) >= 0;
    }

    @Nullable
    public static String getMonth(@Nullable DateTime time) {
        SimpleDateFormat simple = new SimpleDateFormat("MMM");
        Date result = new Date(time.getMillis());
        return simple.format(result);
    }

    @Nullable
    public static int getMonth(@Nullable Long timeInMillis) {
        if (timeInMillis != null) {
            // joda considers JANUARY as 1
            return new DateTime(timeInMillis).getMonthOfYear() - 1;
        }
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    public static int getYear(@Nullable Long selectedDateInMillis) {
        if (selectedDateInMillis != null) {
            return new DateTime(selectedDateInMillis).getYear();
        }
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static String getFormat3(@Nullable DateTime activeDate) {
        if (activeDate == null) {
            return null;
        }

        if (isCurrentDate(activeDate)) {
            return "On Date";
        }
        if (isDatePassed(activeDate)) {
            return "Crossed";
        }
        return "Upcoming";
    }

    public static boolean isCurrentDateCrossed(DateTime activeDate) {
        DateTime present = new DateTime();
        if (activeDate.getYear() == present.getYear()) {
            return activeDate.getMonthOfYear() >= present.getMonthOfYear();
        }
        return activeDate.getYear() > present.getYear();
    }

    public static boolean isCurrentDate(DateTime activeDate) {
        DateTime present = new DateTime();
        return present.withTimeAtStartOfDay().getMillis()
                == activeDate.withTimeAtStartOfDay().getMillis();
    }

    public static int getFifthDateFromNow() {
        return getDateAfter(5);
    }

    private static int getDateAfter(int days) {
        return DateTime.now().plusDays(days).get(DateTimeFieldType.dayOfMonth());
    }

    public static long getFifthDateTimeInMillis() {
        return getMillisAfter(5);
    }

    private static long getMillisAfter(int days) {
        return DateTime.now().plusDays(days).getMillis();
    }

    public static int getFifteenDateFromNow() {
        return getDateAfter(15);
    }

    public static long getFifteenDateTimeInMillis() {
        return getMillisAfter(15);
    }

    public static int getTenthDateFromNow() {
        return getDateAfter(10);
    }

    public static long getTenthDateTimeInMillis() {
        return getMillisAfter(10);
    }

    public static int getThirtyDateFromNow() {
        return getDateAfter(30);
    }

    public static long getThirtyDateTimeInMillis() {
        return getMillisAfter(30);
    }

    public static boolean isSameDay(String previousTime, String currentTime) {
        return (new DateTime(Long.valueOf(previousTime)))
                .withTimeAtStartOfDay()
                .isEqual((new DateTime(Long.valueOf(currentTime)).withTimeAtStartOfDay()));
    }

    public static boolean isTimeWithinLast10Second(String appCreateTime) {
        DateTime currentTime = new DateTime();
        return currentTime.minusSeconds(10).isBefore(new DateTime(Long.parseLong(appCreateTime)));
    }

    public static Long getRandomHour() {
        return (long) new SecureRandom().nextInt(HOURS_IN_DAY);
    }

    // "convert Sep 2020 to datetime"
    public static DateTime stringToDateTime(String time) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MMM yyyy");
        return dtf.parseDateTime(time);
    }
}
