package `in`.okcredit.merchant.core.store.database

import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.model.Customer
import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(baseQuery)
data class CustomerWithTransactionsInfo(
    val id: String,
    val customerSyncStatus: Int,
    var status: Int,
    var mobile: String?,
    var description: String,
    var createdAt: Timestamp,
    var txnStartTime: Timestamp?,
    var accountUrl: String?,
    var balance: Long,
    var transactionCount: Long,
    var lastActivity: Timestamp?,
    var lastPayment: Timestamp?,
    var profileImage: String?,
    var address: String?,
    var email: String?,
    var newActivityCount: Long = 0,
    var addTransactionRestricted: Boolean = false,
    var registered: Boolean,
    var lastBillDate: Timestamp?,
    var txnAlertEnabled: Boolean,
    var lang: String?,
    var reminderMode: String?,
    var isLiveSales: Boolean,
    var lastActivityMetaInfo: Int = 0,
    var lastAmount: Long = 0,
    var lastViewTime: Timestamp?,
    var blockedByCustomer: Boolean = false,
    var state: Int = Customer.State.ACTIVE.code,
    var restrictContactSync: Boolean = false,
    @ColumnInfo(index = true) val businessId: String,
    val lastReminderSendTime: Timestamp,
)

@Suppress("LongLine")
const val baseQuery = """
        SELECT
            Customer.id,
            Customer.customerSyncStatus,
            Customer.status,
            Customer.mobile,
            Customer.description,
            Customer.createdAt,
            agg.balance as balance,
            agg.transactionCount as transactionCount,
            agg.lastActivity as lastActivity,
            agg.lastPayment as lastPayment,
            Customer.accountUrl,
            Customer.profileImage,
            Customer.address,
            Customer.email,
            MAX(`Transaction`.billDate ) as lastBillDate,
            agg.newActivityCount as newActivityCount,
            Customer.lastViewTime,
            Customer.registered,
            Customer.txnAlertEnabled,
            Customer.lang,
            Customer.reminderMode,
            Customer.txnStartTime,
            Customer.isLiveSales,
            Customer.addTransactionRestricted,
            Customer.blockedByCustomer,
            Customer.state,
            Customer.restrictContactSync,
            Customer.businessId as businessId,
            Customer.lastReminderSendTime as lastReminderSendTime,
            MAX(Case when `Transaction`.isdeleted == 1 then (case when `Transaction`.type == 1 then 0  when `Transaction`.category == 1 then 6 else 1 end) when `Transaction`.isdeleted == 0 then (case  when `Transaction`.amountUpdated == 1 then (case  when `Transaction`.type == 1 then 8 else 9 end)  when `Transaction`.type == 1 then 2 when `Transaction`.state == 0 then 5 when `Transaction`.category == 1 then 7 else 3 end) else 4 end) as lastActivityMetaInfo,
            `Transaction`.amount as lastAmount
        FROM Customer
        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
        LEFT JOIN (
            SELECT
                    Customer.id     as id,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.state == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amount ELSE `Transaction`.amount END) as balance,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN (case when `Transaction`.amountUpdatedAt >= `Transaction`.createdAt then `Transaction`.amountUpdatedAt else `Transaction`.createdAt end) ELSE Customer.createdAt END) as lastActivity,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,
                    MAX(`Transaction`.billDate) as lastBillDate,
                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount
                FROM Customer
                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
                GROUP BY Customer.id) as agg on agg.id = Customer.id
        where (`Transaction`.deleteTime == agg.lastactivity) or (`Transaction`.createdAt == agg.lastactivity) or (customer.createdAt == agg.lastactivity) or (`Transaction`.amountUpdatedAt == agg.lastactivity)
        group by customer.id
    """
