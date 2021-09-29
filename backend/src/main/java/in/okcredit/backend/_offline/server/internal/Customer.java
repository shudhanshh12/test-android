package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

public final class Customer {
    @SerializedName("id")
    public final String id;

    @SerializedName("status")
    public final int status;

    @SerializedName("user_id")
    public final String userId;

    @SerializedName("mobile")
    public final String mobile;

    @SerializedName("description")
    public final String description;

    @SerializedName("created_at")
    public final DateTime createdAt;

    @SerializedName("txn_start_time")
    public final Long txnStartTime;

    @SerializedName("updated_at")
    public final DateTime updatedAt;

    @SerializedName("balance")
    public final float balance;

    @SerializedName("balance_v2")
    public final long balanceV2;

    @SerializedName("tx_count")
    public final long transactionCount;

    @SerializedName("last_activity")
    public final DateTime lastActivity;

    @SerializedName("last_payment")
    public final DateTime lastPayment;

    @SerializedName("account_url")
    public final String accountUrl;

    @SerializedName("profile_image")
    public final String profileImage;

    @SerializedName("address")
    public final String address;

    @SerializedName("email")
    public final String email;

    @SerializedName("registered")
    public final boolean registered;

    @SerializedName("txn_alert_enabled")
    public final boolean txnAlertEnabled;

    @SerializedName("lang")
    public final String lang;

    @SerializedName("reminder_mode")
    public final String reminderMode;

    @SerializedName("due_custom_date")
    public final DateTime dueCustomDate;

    @SerializedName("is_live_sales")
    public final boolean isLiveSales;

    @SerializedName("add_transaction_restricted")
    public final boolean addTransactionRestricted;

    @SerializedName("state")
    public final int state;

    @SerializedName("blocked_by_customer")
    public final boolean blockedByCustomer;

    @SerializedName("restrict_contact_sync")
    public final boolean restrictContactSync;

    @SerializedName("due_reminder_enabled_set")
    public boolean dueReminderEnabledSet;

    @SerializedName("due_credit_period_set")
    public boolean dueCreditPeriodSet;

    @SerializedName("last_reminder_sent")
    public final DateTime lastReminderSendTime;

    public Customer(
            String id,
            int status,
            String userId,
            String mobile,
            String description,
            DateTime createdAt,
            Long txnStartTime,
            DateTime updatedAt,
            float balance,
            long balanceV2,
            long transactionCount,
            DateTime lastActivity,
            DateTime lastPayment,
            String accountUrl,
            String profileImage,
            String address,
            String email,
            boolean registered,
            boolean txnAlertEnabled,
            String lang,
            String reminderMode,
            boolean isLiveSales,
            DateTime dueCustomDate,
            boolean dueReminderEnabledSet,
            boolean dueCreditPeriodSet,
            boolean addTransactionRestricted,
            int state,
            boolean blockedByCustomer,
            boolean restrictContactSync,
            DateTime lastReminderSendTime) {

        this.id = id;
        this.status = status;
        this.userId = userId;
        this.accountUrl = accountUrl;
        this.mobile = mobile;
        this.description = description;
        this.createdAt = createdAt;
        this.txnStartTime = txnStartTime;
        this.updatedAt = updatedAt;
        this.balance = balance;
        this.balanceV2 = balanceV2;
        this.transactionCount = transactionCount;
        this.lastActivity = lastActivity;
        this.lastPayment = lastPayment;
        this.profileImage = profileImage;
        this.address = address;
        this.email = email;
        this.registered = registered;
        this.txnAlertEnabled = txnAlertEnabled;
        this.lang = lang;
        this.reminderMode = reminderMode;
        this.dueCustomDate = dueCustomDate;
        this.dueReminderEnabledSet = dueReminderEnabledSet;
        this.dueCreditPeriodSet = dueCreditPeriodSet;
        this.isLiveSales = isLiveSales;
        this.addTransactionRestricted = addTransactionRestricted;
        this.state = state;
        this.blockedByCustomer = blockedByCustomer;
        this.restrictContactSync = restrictContactSync;
        this.lastReminderSendTime = lastReminderSendTime;
    }
}
