package `in`.okcredit.merchant.core.model

import `in`.okcredit.merchant.core.common.Timestamp
import androidx.annotation.Keep

@Keep
data class Customer(
    val id: String,
    val customerSyncStatus: Int,
    val status: Int,
    val mobile: String? = null,
    val description: String,
    val createdAt: Timestamp,
    val txnStartTime: Timestamp? = null,
    val accountUrl: String? = null,
    val balance: Long,
    val transactionCount: Long,
    val lastActivity: Timestamp? = null,
    val lastPayment: Timestamp? = null,
    val profileImage: String? = null,
    val address: String? = null,
    val email: String? = null,
    val newActivityCount: Long = 0,
    val addTransactionPermissionDenied: Boolean = false,
    val registered: Boolean,
    val lastBillDate: Timestamp? = null,
    val txnAlertEnabled: Boolean,
    val lang: String? = null,
    val reminderMode: String? = null,
    val isLiveSales: Boolean,
    var lastActivityMetaInfo: Int?, // todo enum?
    var lastAmount: Long?,
    var lastViewTime: Timestamp? = null,
    var blockedByCustomer: Boolean = false,
    var state: State = State.ACTIVE,
    var restrictContactSync: Boolean = false,
    val lastReminderSendTime: Timestamp = Timestamp(0)
) {
    enum class State(var code: Int) {
        BLOCKED(3), ACTIVE(1);

        companion object {
            fun getState(code: Int) = when (code) {
                BLOCKED.code -> BLOCKED
                ACTIVE.code -> ACTIVE
                else -> ACTIVE
            }
        }
    }

    enum class CustomerSyncStatus(
        val code: Int,
    ) {
        CLEAN(0), // Customer has been synced with the backend
        DIRTY(1), // Customer has been added offline and pending sync
        IMMUTABLE(3); // A conflicting account exists on backend with different account number

        companion object {
            fun from(value: Int) = when (value) {
                CLEAN.code -> CLEAN
                DIRTY.code -> DIRTY
                IMMUTABLE.code -> IMMUTABLE
                else -> CLEAN
            }
        }
    }
}
