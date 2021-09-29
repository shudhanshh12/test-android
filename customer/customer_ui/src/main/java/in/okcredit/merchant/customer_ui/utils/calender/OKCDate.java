package in.okcredit.merchant.customer_ui.utils.calender;

import org.joda.time.DateTime;

public class OKCDate {
    private int data = 0;
    private boolean isSelected = false;
    private long timeInMillis;
    private int status = 0;

    // UNKNOWN = 0;
    // ACTIVE = 1;
    // INVALIDATED = 2;
    private int invalidationReason = 0;

    // NOT_KNOWN = 0;
    // When Payment has been made and balance is non-negative
    // ADVANCED = 1;
    // When both Credit Period and Custom Date is off
    // RESET = 2;
    private boolean dueReminderSent = false;

    public OKCDate(int data, long timeInMillis) {
        this.data = data;
        this.timeInMillis = timeInMillis;
    }

    public boolean isDueReminderSent() {
        return dueReminderSent;
    }

    public void setDueReminderSent(boolean dueReminderSent) {
        this.dueReminderSent = dueReminderSent;
    }

    public int getInvalidationReason() {
        return invalidationReason;
    }

    public void setInvalidationReason(int invalidationReason) {
        this.invalidationReason = invalidationReason;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OKCDate)) return false;

        OKCDate okcDate = (OKCDate) o;

        return getTimeInMillis() == okcDate.getTimeInMillis();
    }

    @Override
    public int hashCode() {
        return getData();
    }

    public boolean isFutureDate() {
        long currentMillis = new DateTime().withTimeAtStartOfDay().getMillis();
        return currentMillis < timeInMillis;
    }
}
