package `in`.okcredit.merchant.core.store.database

import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.model.Customer
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity
data class Transaction(
    @PrimaryKey
    val id: String,
    val type: Int,
    val customerId: String,
    val amount: Long,
    val collectionId: String?,
    val images: String?,
    val note: String?,
    val createdAt: Timestamp,
    val isDeleted: Boolean,
    val deleteTime: Timestamp? = null,
    val isDirty: Boolean,
    val billDate: Timestamp,
    val updatedAt: Timestamp,
    val smsSent: Boolean,
    val createdByCustomer: Boolean,
    val deletedByCustomer: Boolean,
    val inputType: String? = null,
    val voiceId: String? = null,
    val state: Int,
    val category: Int,
    val amountUpdated: Boolean,
    val amountUpdatedAt: Timestamp? = null,
    @ColumnInfo(index = true) val businessId: String,
)

@Entity(indices = [Index(value = ["commandId"], unique = true)])
data class Command(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val commandId: String,
    val type: Command.CommandType,
    val value: String,
    val timestamp: Timestamp,
    val transactionId: String,
    val customerId: String,
    @ColumnInfo(index = true) val businessId: String,
)

@Entity
data class Customer(
    @PrimaryKey var id: String,
    val customerSyncStatus: Int,
    var status: Int,
    var mobile: String? = null,
    var description: String,
    var createdAt: Timestamp,
    var txnStartTime: Timestamp? = null,
    var accountUrl: String? = null,
    var balance: Long,
    var transactionCount: Long,
    var lastActivity: Timestamp? = null,
    var lastPayment: Timestamp? = null,
    var profileImage: String? = null,
    var address: String? = null,
    var email: String? = null,
    var newActivityCount: Long = 0,
    var addTransactionRestricted: Boolean = false,
    var registered: Boolean,
    var lastBillDate: Timestamp? = null,
    var txnAlertEnabled: Boolean,
    var lang: String? = null,
    var reminderMode: String? = null,
    var isLiveSales: Boolean,
    var lastViewTime: Timestamp? = null,
    var blockedByCustomer: Boolean = false,
    var state: Int = Customer.State.ACTIVE.code,
    var restrictContactSync: Boolean = false,
    val lastReminderSendTime: Timestamp? = null,
    @ColumnInfo(index = true) val businessId: String,
)

data class BulkReminderDbInfo(
    val totalBalanceDue: Long,
    val countNumberOfCustomers: Int,
    val totalCustomers: Int,
)

data class CoreDbReminderProfile(
    val id: String,
    val businessId: String,
    val description: String,
    val profileImage: String? = null,
    val balance: Long,
    val lastPayment: Timestamp? = null,
    val lastReminderSendTime: Timestamp? = null,
    val reminderMode: String? = null,
    val firstTxnTime: Timestamp? = null,
    val dueSinceTime: Timestamp? = null
)

@Entity
data class SuggestedCustomerIdsForAddTransaction(
    @PrimaryKey
    val id: String,
    @ColumnInfo(index = true) val businessId: String,
)
