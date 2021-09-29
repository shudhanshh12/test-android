package `in`.okcredit.merchant.suppliercredit.store.database

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import org.joda.time.DateTime

@DatabaseView(baseQuery)
data class SupplierWithTransactionsInfo(
    val id: String,
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
    var restrictContactSync: Boolean,
    @ColumnInfo(index = true) var businessId: String
)

// conditions to show unread transaction count ('newActivityCount')
// 1. transaction created time must be > than lastViewTime
// 2. transaction must be created by other merchant (supplier)
// 3. transaction should not be 'deleted' type (must be 'added' type transaction)

const val baseQuery = """
        SELECT
            supplier.id,
            supplier.registered,
            supplier.deleted,
            supplier.createTime,
            supplier.txnStartTime,
            supplier.name,
            supplier.mobile,
            supplier.address,
            supplier.profileImage,
            supplier.restrictContactSync,
            SUM(CASE WHEN `TRANSACTION`.deleted == 1 THEN 0 WHEN `Transaction`.transactionState == 0 THEN 0 WHEN `TRANSACTION`.payment == 0 THEN -1 * `TRANSACTION`.amount ELSE `TRANSACTION`.amount END) as balance,
            SUM(CASE WHEN supplier.lastViewTime == 0 THEN 0 WHEN `TRANSACTION`.updateTime > supplier.lastViewTime AND `TRANSACTION`.createdBySupplier == 1 AND `TRANSACTION`.deleted == 0  THEN 1 ELSE 0 END) as newActivityCount,
            MAX(`TRANSACTION`.updateTime ) as lastActivityTime,
            supplier.lastViewTime,
            supplier.txnAlertEnabled,
            supplier.lang,
            MAX(`TRANSACTION`.syncing ) as syncing,
            MAX(`TRANSACTION`.lastSyncTime ) as lastSyncTime,
            supplier.addTransactionRestricted,
            supplier.state,
            supplier.blockedBySupplier,
            supplier.businessId as businessId
        FROM Supplier
        LEFT OUTER JOIN `TRANSACTION` ON Supplier.id = `TRANSACTION`.supplierId
        GROUP BY Supplier.id
    """
