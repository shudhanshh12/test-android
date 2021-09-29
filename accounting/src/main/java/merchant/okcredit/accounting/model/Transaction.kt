package merchant.okcredit.accounting.model

import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils
import java.io.Serializable

data class Transaction(
    val id: String,
    val type: Int,
    val customerId: String,
    val collectionId: String?,
    val amountV2: Long,
    val receiptUrl: List<TransactionImage>?,
    val note: String?,
    val createdAt: DateTime,
    val isOnboarding: Boolean,
    val isDeleted: Boolean,
    val deleteTime: DateTime?,
    val isDirty: Boolean,
    val billDate: DateTime,
    val updatedAt: DateTime,
    val isSmsSent: Boolean,
    val isCreatedByCustomer: Boolean,
    val isDeletedByCustomer: Boolean,
    val inputType: String?,
    val voiceId: String?,
    val transactionState: Int,
    val transactionCategory: Int,
    val amountUpdated: Boolean,
    val amountUpdatedAt: DateTime?
) : Serializable {
    var currentDue: Long = 0

    fun lastActivity(): DateTime? {
        return if (isDeleted) deleteTime else createdAt
    }

    val isSubscriptionTransaction: Boolean
        get() = transactionCategory == SUBSCRIPTION && collectionId != null && collectionId.isNotEmpty()

    val isOnlinePaymentTransaction: Boolean
        get() = (
            collectionId != null && collectionId.isNotEmpty() &&
                transactionCategory != SUBSCRIPTION
            )

    val isDiscountTransaction: Boolean
        get() = transactionCategory == DISCOUNT

    fun withDirty(isDirty: Boolean): Transaction {
        return Transaction(
            id,
            type,
            customerId,
            collectionId,
            amountV2,
            receiptUrl,
            note,
            createdAt,
            isOnboarding,
            isDeleted,
            deleteTime,
            isDirty,
            billDate,
            updatedAt,
            isSmsSent,
            isCreatedByCustomer,
            isDeletedByCustomer,
            "",
            "",
            transactionState,
            transactionCategory,
            amountUpdated,
            amountUpdatedAt
        )
    }

    fun withSmsSent(smsSent: Boolean): Transaction {
        return Transaction(
            id,
            type,
            customerId,
            collectionId,
            amountV2,
            receiptUrl,
            note,
            createdAt,
            isOnboarding,
            isDeleted,
            deleteTime,
            isDirty,
            billDate,
            updatedAt,
            smsSent,
            isCreatedByCustomer,
            isDeletedByCustomer,
            "",
            "",
            transactionState,
            transactionCategory,
            amountUpdated,
            amountUpdatedAt
        )
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as Transaction
        if (type != that.type) return false
        if (isOnboarding != that.isOnboarding) return false
        if (isDeleted != that.isDeleted) return false
        if (isDirty != that.isDirty) return false
        if (that.currentDue.toFloat().compareTo(currentDue.toFloat()) != 0) return false
        if (id != that.id) return false
        if (customerId != that.customerId) return false
        if (if (receiptUrl != null) receiptUrl != that.receiptUrl else that.receiptUrl != null) return false
        if (if (note != null) note != that.note else that.note != null) return false
        if (createdAt != that.createdAt) return false
        if (createdAt != that.createdAt) return false
        if (transactionState != that.transactionState) return false
        if (transactionCategory != that.transactionCategory) return false
        if (amountUpdated != that.amountUpdated) return false
        return if (deleteTime != null) deleteTime == that.deleteTime else that.deleteTime == null
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type
        result = 31 * result + transactionState
        result = 31 * result + customerId.hashCode()
        result = 31 * result + (receiptUrl?.hashCode() ?: 0)
        result = 31 * result + (note?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + if (isOnboarding) 1 else 0
        result = 31 * result + if (isDeleted) 1 else 0
        result = 31 * result + (deleteTime?.hashCode() ?: 0)
        result = 31 * result + if (isDirty) 1 else 0
        result =
            31 * result + if (currentDue.toFloat() != +0.0f) java.lang.Float.floatToIntBits(currentDue.toFloat()) else 0
        result = 31 * result + transactionCategory
        result = 31 * result + if (amountUpdated) 1 else 0
        result = 31 * result + (amountUpdatedAt?.hashCode() ?: 0)
        return result
    }

    fun asDeleted(): Transaction {
        return Transaction(
            id,
            type,
            customerId,
            collectionId,
            amountV2,
            receiptUrl,
            note,
            createdAt,
            isOnboarding,
            true,
            DateTimeUtils.currentDateTime(),
            isDirty,
            billDate,
            updatedAt,
            isSmsSent,
            isCreatedByCustomer,
            isDeletedByCustomer,
            "",
            "",
            transactionState,
            transactionCategory,
            amountUpdated,
            amountUpdatedAt
        )
    }

    override fun toString(): String {
        return (
            "Transaction{" +
                "inputType='" +
                inputType +
                '\'' +
                ", voiceId='" +
                voiceId +
                '\'' +
                ", id='" +
                id +
                '\'' +
                ", type=" +
                type +
                ", customerId='" +
                customerId +
                '\'' +
                ", collectionId='" +
                collectionId +
                '\'' +
                ", amountV2=" +
                amountV2 +
                ", receiptUrl=" +
                receiptUrl +
                ", note='" +
                note +
                '\'' +
                ", createdAt=" +
                createdAt +
                ", isOnboarding=" +
                isOnboarding +
                ", isDeleted=" +
                isDeleted +
                ", deleteTime=" +
                deleteTime +
                ", isDirty=" +
                isDirty +
                ", currentDue=" +
                currentDue +
                ", billDate=" +
                billDate +
                ", updatedAt=" +
                updatedAt +
                ", smsSent=" +
                isSmsSent +
                ", createdByCustomer=" +
                isCreatedByCustomer +
                ", deletedByCustomer=" +
                isDeletedByCustomer +
                ", transactionState=" +
                transactionState +
                ", transactionCategory=" +
                transactionCategory +
                ", amountUpdated=" +
                amountUpdated +
                ", amountUpdatedAt=" +
                amountUpdatedAt +
                "}"
            )
    }

    companion object {
        const val CREDIT = 1
        const val PAYMENT = 2
        const val RETURN = 3 // Handle same as payment
        const val PROCESSING = 0
        const val CREATED = 1
        const val DEAFULT_CATERGORY = 0
        const val DISCOUNT = 1
        const val SUBSCRIPTION = 2
    }
}
