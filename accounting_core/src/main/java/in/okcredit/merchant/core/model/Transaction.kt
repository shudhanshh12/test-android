package `in`.okcredit.merchant.core.model

import `in`.okcredit.merchant.core.common.Timestamp
import androidx.annotation.Keep

@Keep
data class Transaction(
    val id: String,
    val type: Type,
    val customerId: String,
    val amount: Long,
    val collectionId: String?,
    val images: List<TransactionImage> = listOf(),
    val note: String?,
    val createdAt: Timestamp,
    val isDeleted: Boolean = false,
    val deleteTime: Timestamp? = null,
    val isDirty: Boolean = true,
    val billDate: Timestamp,
    val updatedAt: Timestamp,
    val smsSent: Boolean = false,
    val createdByCustomer: Boolean = false,
    val deletedByCustomer: Boolean = false,
    val inputType: String? = null,
    val voiceId: String? = null,
    val state: State = State.CREATED,
    val category: Category = Category.DEFAULT,
    val amountUpdated: Boolean = false,
    val amountUpdatedAt: Timestamp? = null
) {
    enum class Type(val code: Int) {
        CREDIT(1), PAYMENT(2);

        companion object {
            @JvmStatic
            fun getTransactionType(code: Int) = when (code) {
                CREDIT.code -> CREDIT
                PAYMENT.code -> PAYMENT
                else -> PAYMENT
            }
        }
    }

    enum class State(val code: Int) {
        PROCESSING(0), CREATED(1), DELETED(2);

        companion object {
            fun getTransactionState(code: Int) = when (code) {
                PROCESSING.code -> PROCESSING
                CREATED.code -> CREATED
                DELETED.code -> DELETED
                else -> CREATED
            }
        }
    }

    enum class Category(val code: Int) {
        DEFAULT(0), DISCOUNT(1), AUTO_CREDIT(2);

        companion object {
            fun getTransactionCategory(code: Int) = when (code) {
                DEFAULT.code -> DEFAULT
                DISCOUNT.code -> DISCOUNT
                AUTO_CREDIT.code -> AUTO_CREDIT
                else -> DEFAULT
            }
        }
    }
}
