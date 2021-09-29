package `in`.okcredit.merchant.suppliercredit

import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.isNotNullOrBlank

data class Supplier(
    val id: String,
    val registered: Boolean = false,
    val deleted: Boolean = false,
    val createTime: DateTime,
    val txnStartTime: Long,

    // profile
    val name: String,
    val mobile: String? = null,
    val address: String? = null,
    val profileImage: String? = null,

    // account
    val balance: Long = 0,

    // notification
    val newActivityCount: Long = 0,
    val lastActivityTime: DateTime? = null,
    val lastViewTime: DateTime? = null,

    // preferences
    val txnAlertEnabled: Boolean = true,
    val lang: String? = null,

    // sync
    val syncing: Boolean = false,

    val lastSyncTime: DateTime? = null,

    val addTransactionRestricted: Boolean = false,

    var state: Int = ACTIVE,

    val blockedBySupplier: Boolean = false,
    val restrictContactSync: Boolean,

) {
    override fun equals(other: Any?): Boolean {
        if (other != Supplier) return false
        if (other is Supplier) {
            return EssentialData(this) == EssentialData(other)
        }
        return false
    }

    override fun hashCode() = EssentialData(this).hashCode()
    override fun toString() = EssentialData(this).toString().replaceFirst("EssentialData", "Supplier")

    companion object {
        const val BLOCKED = 3
        const val ACTIVE = 1
    }
}

private data class EssentialData(val lastSyncTime: DateTime?) {
    constructor(supplier: Supplier) : this(lastSyncTime = supplier.lastSyncTime)
}

data class Transaction(
    val id: String,
    val supplierId: String,
    val collectionId: String?,

    val payment: Boolean = false,
    val amount: Long,
    val note: String? = null,
    val receiptUrl: String? = null,
    val billDate: DateTime,

    val createTime: DateTime,
    val createdBySupplier: Boolean = false,

    val deleted: Boolean = false,
    val deleteTime: DateTime? = null,
    val deletedBySupplier: Boolean = false,

    val updateTime: DateTime,

    // true = already synced
    val syncing: Boolean = false,
    internal val lastSyncTime: DateTime? = null,
    val transactionState: Int = -1,
    val tx_category: Int = -1,
    var blindPay: Boolean = false,
    var finalReceiptUrl: String? = null, // final path after searching for local copy
) {
    object Constants {

        const val PROCESSING = 0
        const val CREDIT = 1
        const val PAYMENT = 2
        const val RETURN = 3 // Handle same as payment
    }

    // tx_Category 2 is subscription transactions with collection id as non null
    fun isOnlineTransaction() = collectionId.isNotNullOrBlank() && tx_category != 2
}

data class AccountMetaInfo(
    val accountIdsListWithFeatureEnabled: List<String>,
    val accountIdsListWithRestrictTxnEnabled: List<String>,
    val accountIdsListWithAddTransactionRestricted: List<String>,
)
