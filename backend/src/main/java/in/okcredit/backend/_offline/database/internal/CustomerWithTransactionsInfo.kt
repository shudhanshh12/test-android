package `in`.okcredit.backend._offline.database.internal

import `in`.okcredit.backend.contract.Customer
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import org.joda.time.DateTime

@DatabaseView(baseQuery)
data class CustomerWithTransactionsInfo(
    val id: String,
    var status: Int,
    var mobile: String?,
    var description: String,
    var createdAt: DateTime,
    var balance: Long,
    var balanceV2: Long,
    var transactionCount: Long,
    var lastActivity: DateTime?,
    var lastPayment: DateTime?,
    var accountUrl: String?,
    var profileImage: String?,
    var address: String?,
    var email: String?,
    var lastBillDate: DateTime?,
    var newActivityCount: Long = 0,
    var lastViewTime: DateTime?,
    var registered: Boolean,
    var txnAlertEnabled: Boolean,
    var lang: String?,
    var reminderMode: String?,
    var txnStartTime: Long?,
    var isLiveSales: Boolean,
    var isDueActive: Boolean = false,
    var activeDate: DateTime?,
    var isCustomDateSet: Boolean = false,
    var lastActivityMetaInfo: Int = 0,
    var lastAmount: Long = 0,
    var addTransactionRestricted: Boolean = false,
    var blockedByCustomer: Boolean = false,
    var state: Int = Customer.State.ACTIVE.value,
    var restrictContactSync: Boolean = false,
    @ColumnInfo(index = true) var businessId: String,
    val lastReminderSendTime: DateTime?,
)

const val baseQuery = """
        SELECT
            Customer.id,
            Customer.status,
            Customer.mobile,
            Customer.description,
            Customer.createdAt,
            0 as balance,
            agg.balanceV2 as balanceV2,
            agg.transactionCount as transactionCount,
            agg.lastActivity as lastActivity,
            agg.lastPayment as lastPayment,
            DueInfo.is_due_active as isDueActive,
            DueInfo.active_date as activeDate,
            DueInfo.is_custom_date_set as isCustomDateSet,
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
            Customer.restrictContactSync,
            Customer.state,
            Customer.businessId as businessId,
            Customer.lastReminderSendTime,
            MAX(Case when `Transaction`.isdeleted ==1 then (case when `Transaction`.type==1 then 0  when `Transaction`.transactionCategory==1 then 6 else 1 end) when `Transaction`.isdeleted == 0 then (case  when `Transaction`.amountUpdated==1 then (case  when `Transaction`.type==1 then 8 else 9 end)  when `Transaction`.type==1 then 2 when `Transaction`.transactionState == 0 then 5 when `Transaction`.transactionCategory==1 then 7 else 3 end) else 4 end) as lastActivityMetaInfo,
            `Transaction`.amountV2 as lastAmount
        FROM Customer
        LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
        LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId
        LEFT JOIN (
            SELECT
                    Customer.id     as id,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.transactionState == 0 THEN 0 WHEN `Transaction`.type == 1 THEN -1 * `Transaction`.amountV2 ELSE `Transaction`.amountV2 END) as balanceV2,
                    SUM(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 ELSE 1 END) as transactionCount,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN `Transaction`.deleteTime WHEN `Transaction`.createdAt >= Customer.createdAt THEN (case when `Transaction`.amountUpdatedAt >= `Transaction`.createdAt then `Transaction`.amountUpdatedAt else `Transaction`.createdAt end) ELSE Customer.createdAt END) as lastActivity,
                    MAX(CASE WHEN `Transaction`.isDeleted == 1 THEN 0 WHEN `Transaction`.type == 1 THEN 0 ELSE `Transaction`.createdAt END) as lastPayment,
                    MAX(`Transaction`.billDate ) as lastBillDate,
                    SUM(CASE WHEN Customer.lastViewTime == 0 THEN 0 WHEN `Transaction`.updatedAt > Customer.lastViewTime AND `Transaction`.createdByCustomer == 1  THEN 1 ELSE 0 END) as newActivityCount
                FROM Customer
                LEFT OUTER JOIN `Transaction` ON Customer.id = `Transaction`.customerId
                LEFT JOIN `DueInfo` ON Customer.id = `DueInfo`.customerId
                GROUP BY Customer.id) as agg on agg.id=Customer.id
        where (`Transaction`.deleteTime==agg.lastactivity) or (`Transaction`.createdAt==agg.lastactivity) or (customer.createdAt==agg.lastactivity)  or (`Transaction`.amountUpdatedAt==agg.lastactivity)
        group by customer.id
    """
