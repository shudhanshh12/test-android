package `in`.okcredit.backend.contract

import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.CLEAN
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.common.base.Strings
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Keep
@Parcelize
data class Customer(
    val id: String,
    val customerSyncStatus: Int = CLEAN.code,
    val status: Int = 0,
    val mobile: String? = null,
    val description: String,
    val createdAt: DateTime? = null,
    val txnStartTime: Long? = null,
    val balanceV2: Long = 0,
    val transactionCount: Long = 0,
    val lastActivity: DateTime? = null,
    val lastPayment: DateTime? = null,
    val accountUrl: String? = null,
    val profileImage: String? = null,
    val address: String? = null,
    val email: String? = null,
    val newActivityCount: Long = 0,
    var lastViewTime: DateTime? = null,
    val registered: Boolean = false,
    private val lastBillDate: DateTime? = null,
    val txnAlertEnabled: Boolean = false,
    val lang: String? = null,
    val reminderMode: String? = null,
    val isLiveSales: Boolean = false,
    val addTransactionPermissionDenied: Boolean = false,
    val state: State? = null,
    val blockedByCustomer: Boolean = false,
    val restrictContactSync: Boolean = false,
    val dueActive: Boolean = false,
    val dueInfo_activeDate: DateTime? = null,
    val customDateSet: Boolean = false,
    val lastActivityMetaInfo: Int? = null,
    val lastAmount: Long? = null,
    val dueWarningDrawable: Int = 0,
    val dueCreditPeriodSet: Boolean = false,
    val dueReminderEnabledSet: Boolean = false,
    val lastReminderSendTime: DateTime
) : Parcelable {

    enum class State(val value: Int) {
        BLOCKED(3), ACTIVE(1);
    }

    fun getLastBillDate(): DateTime? {
        return lastBillDate ?: lastActivity
    }

    fun canSendCollectionLink(): Boolean {
        return !Strings.isNullOrEmpty(mobile) && balanceV2 < -1000 && balanceV2 > -10000000 && status == 1
    }

    fun isActive() = status == State.ACTIVE.value

    fun isTxnAlertEnabled() = txnAlertEnabled

    fun isRegistered() = registered

    fun isAddTransactionPermissionDenied() = addTransactionPermissionDenied

    fun isBlockedByCustomer() = blockedByCustomer

    fun isRestrictContactSync() = restrictContactSync

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
