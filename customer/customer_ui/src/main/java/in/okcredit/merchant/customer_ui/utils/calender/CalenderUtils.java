package in.okcredit.merchant.customer_ui.utils.calender;

import java.util.Calendar;

public class CalenderUtils {

    static String getMonth(int month) {
        switch (month) {
            case (Calendar.JANUARY):
                return "Januay";
            case (Calendar.FEBRUARY):
                return "February";
            case (Calendar.MARCH):
                return "March";
            case (Calendar.APRIL):
                return "April";
            case (Calendar.MAY):
                return "May";
            case (Calendar.JUNE):
                return "June";
            case (Calendar.JULY):
                return "July";
            case (Calendar.AUGUST):
                return "August";
            case (Calendar.SEPTEMBER):
                return "September";
            case (Calendar.OCTOBER):
                return "October";
            case (Calendar.NOVEMBER):
                return "November";
            case (Calendar.DECEMBER):
                return "December";
        }
        return "NULL";
    }

    static int getMonthInInt(String month) {
        switch (month) {
            case "Jan":
                return (Calendar.JANUARY);
            case "Feb":
                return (Calendar.FEBRUARY);
            case "Mar":
                return (Calendar.MARCH);
            case "Apr":
                return (Calendar.APRIL);
            case "May":
                return (Calendar.MAY);
            case "Jun":
                return (Calendar.JUNE);
            case "Jul":
                return (Calendar.JULY);
            case "Aug":
                return (Calendar.AUGUST);
            case "Sep":
                return (Calendar.SEPTEMBER);
            case "Oct":
                return (Calendar.OCTOBER);
            case "Nov":
                return (Calendar.NOVEMBER);
            case "Dec":
                return (Calendar.DECEMBER);
        }
        return -1;
    }

    static String getDay(int day) {
        switch (day) {
            case (Calendar.SUNDAY):
                return "Sun";
            case (Calendar.MONDAY):
                return "Mon";
            case (Calendar.TUESDAY):
                return "Tue";
            case (Calendar.WEDNESDAY):
                return "Wed";
            case (Calendar.THURSDAY):
                return "Thu";
            case (Calendar.FRIDAY):
                return "Fri";
            case (Calendar.SATURDAY):
                return "Sat";
        }
        return "NULL";
    }
}
