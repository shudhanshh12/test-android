package `in`.okcredit.merchant.suppliercredit.store.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity
data class Supplier(
    @PrimaryKey
    var id: String,
    var registered: Boolean,
    var deleted: Boolean,
    var createTime: DateTime,
    var txnStartTime: Long,
    var name: String,
    var mobile: String?,
    var address: String?,
    var profileImage: String?,
    var balance: Long,
    var newActivityCount: Long,
    var lastActivityTime: DateTime?,
    var lastViewTime: DateTime?,
    var txnAlertEnabled: Boolean,
    var lang: String?,
    var syncing: Boolean,
    var lastSyncTime: DateTime?,
    var addTransactionRestricted: Boolean,
    var state: Int,
    var blockedBySupplier: Boolean,
    val restrictContactSync: Boolean,
    @ColumnInfo(index = true) val businessId: String,
)

@Entity
data class Transaction(
    @PrimaryKey
    var id: String,
    var supplierId: String,
    var collectionId: String?,
    var payment: Boolean,
    var amount: Long,
    var note: String?,
    var receiptUrl: String?,
    var billDate: DateTime,
    var createTime: DateTime,
    var createdBySupplier: Boolean,
    var deleted: Boolean,
    var deleteTime: DateTime?,
    var deletedBySupplier: Boolean,
    var updateTime: DateTime,
    var syncing: Boolean,
    var lastSyncTime: DateTime?,
    val transactionState: Int = -1,
    val txCategory: Int = 0,
    @ColumnInfo(index = true) val businessId: String,
)

@Entity
data class NotificationReminder(
    @PrimaryKey
    var id: String,
    var accountId: String,
    var createdAt: String,
    var expiresAt: String,
    var status: Int,
    @ColumnInfo(index = true) val businessId: String,
)
