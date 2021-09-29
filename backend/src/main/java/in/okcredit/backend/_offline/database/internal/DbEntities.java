package in.okcredit.backend._offline.database.internal;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

public interface DbEntities {

    @Entity
    final class Merchant {
        @NonNull @PrimaryKey public String id;
        public String name;
        public String mobile;
        public DateTime createdAt;
        public String profileImage;
        public String address;
        public Double addressLatitude;
        public Double addressLongitude;
        public String about;
        public String email;
        public String contactName;
        public String upiVpa;
    }

    @Entity(primaryKeys = {"merchantId", "key"})
    final class MerchantPreference {
        @NonNull public String merchantId;
        @NonNull public String key;
        @NonNull public String value;
    }

    @Entity
    final class Customer {
        @NonNull @PrimaryKey public String id;
        public int status;
        public String mobile;
        public String description;
        public DateTime createdAt;
        public float balance;
        public long balanceV2;
        public long transactionCount;
        public DateTime lastActivity;
        public DateTime lastPayment;
        public String accountUrl;
        public String profileImage;
        public String address;
        public String email;
        public DateTime lastBillDate;
        public long newActivityCount;
        public DateTime lastViewTime;
        public boolean registered;
        public boolean txnAlertEnabled;
        public String lang;
        public String reminderMode;
        public Long txnStartTime;
        public boolean isLiveSales;
        public boolean addTransactionRestricted;
        public boolean blockedByCustomer;
        public int state;
        public boolean restrictContactSync;
        @NonNull @ColumnInfo(index = true) public String businessId;
        public DateTime lastReminderSendTime;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Customer customer = (Customer) o;

            if (status != customer.status) return false;
            if (Float.compare(customer.balance, balance) != 0) return false;
            if (balanceV2 != customer.balanceV2) return false;
            if (transactionCount != customer.transactionCount) return false;
            if (newActivityCount != customer.newActivityCount) return false;
            if (registered != customer.registered) return false;
            if (txnAlertEnabled != customer.txnAlertEnabled) return false;
            if (!id.equals(customer.id)) return false;
            if (mobile != null ? !mobile.equals(customer.mobile) : customer.mobile != null)
                return false;
            if (description != null
                    ? !description.equals(customer.description)
                    : customer.description != null) return false;
            if (createdAt != null
                    ? !createdAt.equals(customer.createdAt)
                    : customer.createdAt != null) return false;
            if (txnStartTime != null
                    ? !txnStartTime.equals(customer.txnStartTime)
                    : customer.txnStartTime != null) return false;
            if (lastActivity != null
                    ? !lastActivity.equals(customer.lastActivity)
                    : customer.lastActivity != null) return false;
            if (lastPayment != null
                    ? !lastPayment.equals(customer.lastPayment)
                    : customer.lastPayment != null) return false;
            if (accountUrl != null
                    ? !accountUrl.equals(customer.accountUrl)
                    : customer.accountUrl != null) return false;
            if (profileImage != null
                    ? !profileImage.equals(customer.profileImage)
                    : customer.profileImage != null) return false;
            if (address != null ? !address.equals(customer.address) : customer.address != null)
                return false;
            if (email != null ? !email.equals(customer.email) : customer.email != null)
                return false;
            if (lastBillDate != null
                    ? !lastBillDate.equals(customer.lastBillDate)
                    : customer.lastBillDate != null) return false;
            if (lastViewTime != null
                    ? !lastViewTime.equals(customer.lastViewTime)
                    : customer.lastViewTime != null) return false;
            if (lang != null ? !lang.equals(customer.lang) : customer.lang != null) return false;
            if (isLiveSales != customer.isLiveSales) return false;
            if (addTransactionRestricted != customer.addTransactionRestricted) return false;
            if (blockedByCustomer != customer.blockedByCustomer) return false;
            if (lastReminderSendTime != null
                    ? !lastReminderSendTime.equals(customer.lastReminderSendTime)
                    : customer.lastReminderSendTime != null) return false;
            if (state != customer.state) return false;
            if (!businessId.equals(customer.businessId)) return false;
            return reminderMode != null
                    ? reminderMode.equals(customer.reminderMode)
                    : customer.reminderMode == null;
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + status;
            result = 31 * result + (mobile != null ? mobile.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
            result = 31 * result + (txnStartTime != null ? txnStartTime.hashCode() : 0);
            result = 31 * result + (balance != +0.0f ? Float.floatToIntBits(balance) : 0);
            result = 31 * result + (int) (balanceV2 ^ (balanceV2 >>> 32));
            result = 31 * result + (int) (transactionCount ^ (transactionCount >>> 32));
            result = 31 * result + (lastActivity != null ? lastActivity.hashCode() : 0);
            result = 31 * result + (lastPayment != null ? lastPayment.hashCode() : 0);
            result = 31 * result + (accountUrl != null ? accountUrl.hashCode() : 0);
            result = 31 * result + (profileImage != null ? profileImage.hashCode() : 0);
            result = 31 * result + (address != null ? address.hashCode() : 0);
            result = 31 * result + (email != null ? email.hashCode() : 0);
            result = 31 * result + (lastBillDate != null ? lastBillDate.hashCode() : 0);
            result = 31 * result + (int) (newActivityCount ^ (newActivityCount >>> 32));
            result = 31 * result + (lastViewTime != null ? lastViewTime.hashCode() : 0);
            result = 31 * result + (registered ? 1 : 0);
            result = 31 * result + (isLiveSales ? 1 : 0);
            result = 31 * result + (addTransactionRestricted ? 1 : 0);
            result = 31 * result + (blockedByCustomer ? 1 : 0);
            result = 31 * result + (state != -1 ? 1 : 0);
            result = 31 * result + (txnAlertEnabled ? 1 : 0);
            result = 31 * result + (lang != null ? lang.hashCode() : 0);
            result = 31 * result + (reminderMode != null ? reminderMode.hashCode() : 0);
            result = 31 * result + businessId.hashCode();
            result = 31 * result + (lastReminderSendTime != null ? lastReminderSendTime.hashCode() : 0);
            return result;
        }
    }

    @Entity
    final class CustomerSync {
        @NonNull @PrimaryKey public String customerId;
        public DateTime lastSync;
    }

    ///
    @Entity
    final class Transaction {
        @NonNull @PrimaryKey public String id;
        public int type;
        public String customerId;
        public String collectionId;
        public float amount;
        public long amountV2;
        public String receiptUrl;
        public String note;
        public DateTime createdAt;
        public boolean isOnboarding;
        public boolean isDeleted;
        public DateTime deleteTime;
        public boolean isDirty;
        public DateTime billDate;
        public DateTime updatedAt;
        public boolean smsSent;

        public boolean createdByCustomer;
        public boolean deletedByCustomer;
        public String inputType;
        public String voiceId;
        public int transactionState = -1;
        public int transactionCategory;
        public boolean amountUpdated;
        public DateTime amountUpdatedAt;
        @NonNull @ColumnInfo(index = true) public String businessId;
    }

    @Entity(primaryKeys = {"customerId", "key"})
    final class CustomerPreference {
        @NonNull public String customerId;
        @NonNull public String key;
        @NonNull public String value;
        @NonNull public boolean isSynced;
    }

    @Entity(primaryKeys = {"customerId"})
    final class DueInfo {
        @NonNull public String customerId;
        @NonNull public boolean is_due_active;
        public DateTime active_date;
        @NonNull public boolean is_custom_date_set;
        @NonNull public boolean is_auto_generated;
        @NonNull @ColumnInfo(index = true) public String businessId;

        @Override
        public String toString() {
            return "DueInfo{"
                    + "customerId='"
                    + customerId
                    + '\''
                    + ", is_due_active="
                    + is_due_active
                    + ", active_date="
                    + active_date
                    + ", is_custom_date_set="
                    + is_custom_date_set
                    + ", is_auto_generated="
                    + is_auto_generated
                    + ", businessId="
                    + businessId
                    + '}';
        }
    }

    @Entity(primaryKeys = {"customerId", "due_at", "status", "dueReminderSent"})
    public class DueDate {

        @NonNull public String id;
        @NonNull public DateTime due_at;
        public int amount;
        public int invalidation_reason;
        public int status;
        public boolean dueReminderSent;
        public DateTime updatedAt;
        public boolean isCustomDate;
        @NonNull public String customerId;
        //
        //        enum Status {
        //            UNKNOWN = 0;
        //            ACTIVE = 1;
        //            INVALIDATED = 2;
        //        }
        //
        //        enum InvalidationReason {
        //            NOT_KNOWN = 0;
        //
        //            // When Payment has been made and balance is non-negative
        //            ADVANCED = 1;
        //
        //            // When both Credit Period and Custom Date is off
        //            RESET = 2;
        //        }

        @Override
        public String toString() {
            return "DueDate{"
                    + "id='"
                    + id
                    + '\''
                    + ", due_at="
                    + due_at
                    + ", amount="
                    + amount
                    + ", invalidation_reason="
                    + invalidation_reason
                    + ", status="
                    + status
                    + ", dueReminderSent="
                    + dueReminderSent
                    + ", updatedAt="
                    + updatedAt
                    + ", isCustomDate="
                    + isCustomDate
                    + ", customerId='"
                    + customerId
                    + '\''
                    + '}';
        }
    }
}
