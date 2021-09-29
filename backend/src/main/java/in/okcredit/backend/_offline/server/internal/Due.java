package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;
import org.joda.time.DateTime;

public class Due {
    @SerializedName("id")
    public final String id;

    @SerializedName("account_id")
    public final String customerId;

    @SerializedName("due_at")
    public final DateTime due_at;

    @SerializedName("amount")
    public final int amount;

    @SerializedName("invalidation_reason")
    public final int invalidation_reason;

    @SerializedName("status")
    public final int status;

    @SerializedName("due_reminder_sent")
    public final boolean dueRemindeRent;

    @SerializedName("updated_at")
    public final DateTime updatedAt;

    @SerializedName("is_custom_date")
    public boolean isCustomDate;

    public Due(
            String id,
            String customerId,
            DateTime due_at,
            int amount,
            int invalidation_reason,
            int status,
            boolean dueRemindeRent,
            DateTime updatedAt,
            boolean isCustomDate) {
        this.id = id;
        this.customerId = customerId;
        this.due_at = due_at;
        this.amount = amount;
        this.invalidation_reason = invalidation_reason;
        this.status = status;
        this.dueRemindeRent = dueRemindeRent;
        this.updatedAt = updatedAt;
        this.isCustomDate = isCustomDate;
    }

    public String getId() {
        return id;
    }

    public DateTime getDue_at() {
        return due_at;
    }

    public int getAmount() {
        return amount;
    }

    public int getInvalidation_reason() {
        return invalidation_reason;
    }

    public int getStatus() {
        return status;
    }

    public boolean isDueRemindeRent() {
        return dueRemindeRent;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isCustomDate() {
        return isCustomDate;
    }

    public void setCustomDate(boolean customDate) {
        isCustomDate = customDate;
    }
}
