package in.okcredit.backend._offline.server.internal;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import merchant.okcredit.accounting.model.TransactionImage;
import org.joda.time.DateTime;

public final class Transaction {

    @SerializedName("id")
    public final String id;

    @SerializedName("type")
    public final int type;

    @SerializedName("customer_id")
    public final String customerId;

    @SerializedName("collection_id")
    public final String collectionId;

    @SerializedName("amount")
    public final float amount;

    @SerializedName("amount_v2")
    public final long amountV2;

    @SerializedName("images")
    public final List<TransactionImage> transactionImageList;

    @SerializedName("note")
    public final String note;

    @SerializedName("created_at")
    public final DateTime createdAt;

    @SerializedName("onboarding")
    public final boolean isOnboarding;

    @SerializedName("deleted")
    public final boolean isDeleted;

    @SerializedName("delete_time")
    public final DateTime deleteTime;

    @SerializedName("bill_date")
    public final DateTime billDate;

    @SerializedName("update_time")
    public final DateTime updatedAt;

    @SerializedName("created_by_customer")
    public final boolean createdByCustomer;

    @SerializedName("deleted_by_customer")
    public final boolean deletedByCustomer;

    @SerializedName("transaction_state")
    public final int transactionState;

    @SerializedName("tx_category")
    public final int transactionCategory;

    @SerializedName("amount_updated")
    public final boolean amountUpdated;

    @SerializedName("amount_updated_at")
    public final DateTime amountUpdatedAt;

    public Transaction(
            String id,
            int type,
            String customerId,
            String collectionId,
            float amount,
            long amountV2,
            List<TransactionImage> transactionImageList,
            String note,
            DateTime createdAt,
            boolean isOnboarding,
            boolean isDeleted,
            DateTime deleteTime,
            DateTime billDate,
            DateTime updatedAt,
            boolean createdByCustomer,
            boolean deletedByCustomer,
            int transactionState,
            int transactionCategory,
            boolean amountUpdated,
            DateTime amountUpdatedAt) {
        this.id = id;
        this.type = type;
        this.customerId = customerId;
        this.collectionId = collectionId;
        this.amount = amount;
        this.amountV2 = amountV2;
        this.transactionImageList = transactionImageList;
        this.note = note;
        this.createdAt = createdAt;
        this.isOnboarding = isOnboarding;
        this.isDeleted = isDeleted;
        this.deleteTime = deleteTime;
        this.billDate = billDate;
        this.updatedAt = updatedAt;
        this.createdByCustomer = createdByCustomer;
        this.deletedByCustomer = deletedByCustomer;
        this.transactionState = transactionState;
        this.transactionCategory = transactionCategory;
        this.amountUpdated = amountUpdated;
        this.amountUpdatedAt = amountUpdatedAt;
    }
}
