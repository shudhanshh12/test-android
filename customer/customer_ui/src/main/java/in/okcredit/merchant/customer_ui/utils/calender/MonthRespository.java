package in.okcredit.merchant.customer_ui.utils.calender;

import java.util.ArrayList;
import java.util.Calendar;
import org.joda.time.DateTime;

public class MonthRespository {

    private final Calendar mCal;
    private ArrayList<OKCDate> currentMonthData;

    MonthRespository() {
        mCal = Calendar.getInstance();
    }

    OKCMonth getCurrentMonthData() {
        OKCMonth okcMonth = new OKCMonth();
        return setMonthDetails(okcMonth);
    }

    OKCMonth getReleventMonthData(int monthName, int year) {
        OKCMonth okcMonth = new OKCMonth();
        mCal.set(Calendar.MONTH, monthName);
        mCal.set(Calendar.YEAR, year);
        return setMonthDetails(okcMonth);
    }

    OKCMonth setMonthDetails(OKCMonth okcMonth) {
        okcMonth.monthName = CalenderUtils.getMonth(mCal.get(Calendar.MONTH));
        okcMonth.yearName = String.valueOf(mCal.get(Calendar.YEAR));
        okcMonth.dateName = String.valueOf(mCal.get(Calendar.DATE));
        okcMonth.dayName = CalenderUtils.getDay(mCal.get(Calendar.DAY_OF_WEEK));
        okcMonth.shortMonthName = CalenderUtils.getMonth(mCal.get(Calendar.MONTH)).substring(0, 3);
        mCal.set(Calendar.DATE, 1);
        Calendar dummy = Calendar.getInstance();
        dummy.set(Calendar.DATE, 1);
        dummy.set(Calendar.MONTH, mCal.get(Calendar.MONTH));
        dummy.set(Calendar.YEAR, mCal.get(Calendar.YEAR));
        int abc = mCal.get(Calendar.DAY_OF_WEEK);
        int start = 1;
        while (start != abc) {
            okcMonth.dates.add(
                    new OKCDate(
                            -1,
                            new DateTime(mCal.getTimeInMillis())
                                    .withTimeAtStartOfDay()
                                    .getMillis()));
            start++;
        }
        int daysInMonth = mCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            okcMonth.dates.add(
                    new OKCDate(
                            i,
                            new DateTime(dummy.getTimeInMillis())
                                    .withTimeAtStartOfDay()
                                    .getMillis()));
            dummy.add(Calendar.DATE, 1);
        }
        return okcMonth;
    }

    OKCMonth getNextMonthData() {
        OKCMonth okcMonth = new OKCMonth();
        if (mCal.get(Calendar.MONTH) == Calendar.DECEMBER) {
            mCal.add(Calendar.YEAR, 1);
            mCal.set(Calendar.MONTH, Calendar.JANUARY);
        } else mCal.set(Calendar.MONTH, mCal.get(Calendar.MONTH) + 1);

        return setMonthDetails(okcMonth);
    }

    OKCMonth getPreviousMonthData() {
        OKCMonth okcMonth = new OKCMonth();
        if (mCal.get(Calendar.MONTH) == Calendar.JANUARY) {
            mCal.add(Calendar.YEAR, -1);
            mCal.set(Calendar.MONTH, Calendar.DECEMBER);
        } else mCal.set(Calendar.MONTH, mCal.get(Calendar.MONTH) - 1);

        mCal.set(Calendar.DATE, 1);

        return setMonthDetails(okcMonth);
    }

    public class OKCMonth {
        String shortMonthName;
        ArrayList<OKCDate> dates = new ArrayList<>();
        String monthName;
        String yearName;
        String dateName;
        String dayName;
    }

    static class OKCSelectedDate {
        DateTime dueAt;
        int status;
        int invalidationReason;
        boolean dueReminderSent;

        public OKCSelectedDate(
                DateTime dueAt, int status, int invalidationReason, boolean dueReminderSent) {
            this.dueAt = dueAt;
            this.status = status;
            this.invalidationReason = invalidationReason;
            this.dueReminderSent = dueReminderSent;
        }

        public DateTime getDueAt() {
            return dueAt;
        }

        public void setDueAt(DateTime dueAt) {
            this.dueAt = dueAt;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getInvalidationReason() {
            return invalidationReason;
        }

        public void setInvalidationReason(int invalidationReason) {
            this.invalidationReason = invalidationReason;
        }

        public boolean isDueReminderSent() {
            return dueReminderSent;
        }

        public void setDueReminderSent(boolean dueReminderSent) {
            this.dueReminderSent = dueReminderSent;
        }

        @Override
        public String toString() {
            return "OKCSelectedDate{"
                    + "dueAt="
                    + dueAt
                    + ", status="
                    + status
                    + ", invalidationReason="
                    + invalidationReason
                    + ", dueReminderSent="
                    + dueReminderSent
                    + '}';
        }
    }
}
