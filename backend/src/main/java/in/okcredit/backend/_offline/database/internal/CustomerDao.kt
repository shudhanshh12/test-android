package `in`.okcredit.backend._offline.database.internal

import `in`.okcredit.backend._offline.common.DbReminderProfile
import `in`.okcredit.merchant.core.model.bulk_reminder.BackendLastReminderSendTime
import `in`.okcredit.merchant.core.store.database.BulkReminderDbInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime

@Dao
interface CustomerDao {

    @Query("SELECT * FROM Customer WHERE  businessId=:businessId")
    fun getCustomers(businessId: String): Observable<List<DbEntities.Customer>>

    @Query("SELECT count(*) FROM Customer WHERE  businessId=:businessId")
    fun getCustomersCount(businessId: String): Observable<Int>

    @Query("SELECT * FROM CustomerWithTransactionsInfo WHERE  businessId=:businessId ORDER BY description")
    fun listCustomers(businessId: String): Flowable<List<CustomerWithTransactionsInfo>>

    @Query("SELECT * FROM CustomerWithTransactionsInfo WHERE  businessId=:businessId ORDER BY lastPayment desc")
    fun listCustomersByLastPayment(businessId: String): Flowable<List<CustomerWithTransactionsInfo>>

    @Query(
        """
        select * from CustomerWithTransactionsInfo
        WHERE businessId=:businessId AND status == 1 AND mobile IS NOT NULL AND balance < -1000
        AND balance > -10000000 ORDER BY lastPayment asc
        """
    )
    fun getDefaulters(businessId: String): Flowable<List<CustomerWithTransactionsInfo>>

    @Query("SELECT * FROM CustomerWithTransactionsInfo WHERE businessId=:businessId AND status == 1 ORDER BY description")
    fun listActiveCustomers(businessId: String): Flowable<List<CustomerWithTransactionsInfo>>

    @Query("SELECT * FROM CustomerWithTransactionsInfo WHERE businessId=:businessId AND id = :customerId LIMIT 1")
    fun getCustomerWithTransactionInfo(
        businessId: String,
        customerId: String?,
    ): Flowable<CustomerWithTransactionsInfo>

    @Query("SELECT * FROM Customer WHERE id = :customerId LIMIT 1")
    fun getCustomer(customerId: String?): Single<DbEntities.Customer>

    @Query("SELECT count(*) FROM CustomerWithTransactionsInfo where businessId=:businessId AND status == 1")
    fun countActiveCustomers(businessId: String): Observable<Long>

    @Query("SELECT id FROM CustomerWithTransactionsInfo WHERE businessId=:businessId AND status == 1 ORDER BY description")
    fun listActiveCustomersIds(businessId: String): Observable<List<String>>

    @Query("SELECT id FROM Customer where isLiveSales == 1 AND businessId =:businessId")
    fun getLiveSalesCustomerId(businessId: String): Single<String>

    @Query("DELETE FROM customer")
    fun deleteAllCustomers()

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insertCustomer(vararg customer: DbEntities.Customer?)

    @Update
    fun updateCustomer(customer: DbEntities.Customer?): Int

    @Query("UPDATE customer SET lastViewTime=:lastViewTime, newActivityCount = 0  WHERE id = :customerId")
    fun updateLastViewTime(customerId: String?, lastViewTime: DateTime?): Int

    @Transaction
    fun putCustomer(customer: DbEntities.Customer?) {
        if (updateCustomer(customer) != 1) {
            insertCustomer(customer)
        }
    }

    @Transaction
    fun resetCustomerList(vararg customers: DbEntities.Customer?) {
        deleteAllCustomers()
        insertCustomer(*customers)
    }

    @Query("SELECT * FROM CustomerWithTransactionsInfo WHERE businessId =:businessId AND mobile = :mobile LIMIT 1")
    fun findCustomerByMobile(businessId: String, mobile: String?): Single<CustomerWithTransactionsInfo>

    @Query("DELETE  FROM Customer WHERE id = :customerId")
    fun deleteCustomer(customerId: String?)

    @Query("UPDATE customer SET description=:description  WHERE id = :customerId")
    fun updateDescription(description: String?, customerId: String?)

    @Query("UPDATE customer SET addTransactionRestricted=:isDenied WHERE id = :customerId")
    fun updateCustomerAddTransaction(customerId: String?, isDenied: Boolean)

    @Query("select * from CustomerWithTransactionsInfo WHERE businessId =:businessId AND status == 1 AND balance < 0 ORDER BY balance asc")
    fun getCustomersWithBalanceDue(businessId: String): Observable<List<CustomerWithTransactionsInfo>>

    @Query(
        """
        SELECT

        (
        SELECT Sum(balance)
        From CustomerWithTransactionsInfo a
        where balance < 0 AND mobile IS NOT NULL
        AND (
        (lastPayment > 0 AND lastPayment < datetime('now', -:defaulterSince))
        OR
        (lastPayment == 0 AND
        (select createdAt from `transaction` b where
        a.id == b.customerId AND a.businessId == b.businessId order by createdAt ASC limit 1)
        < datetime('now', -:defaulterSince))
        )
        ) as totalBalanceDue,

        (
        SELECT count(*) FROM CustomerWithTransactionsInfo a
        where balance < 0 AND mobile IS NOT NULL AND businessId =:businessId
        AND (
        (lastPayment > 0 AND lastPayment < (1000 * strftime('%s', datetime('now', :defaulterSince))) )
        OR
        (lastPayment == 0 AND
        (select createdAt from `transaction` b where a.id == b.customerId order by createdAt ASC limit 1)
        < (1000 * strftime('%s', datetime('now', :defaulterSince))) )
        )
        AND ((lastReminderSendTime == 0 AND lastReminderSendTime is Null) OR lastReminderSendTime <= (1000 * strftime('%s', datetime('now', '-1 day'))))
        ) as countNumberOfCustomers,

        (
        SELECT count(*) FROM CustomerWithTransactionsInfo a
        where balance < 0 AND mobile IS NOT NULL AND businessId =:businessId
        AND (
        (lastPayment > 0 AND lastPayment < (1000 * strftime('%s', datetime('now', :defaulterSince))) )
        OR
        (lastPayment == 0 AND
        (select createdAt from `Transaction` b
        where a.id == b.customerId AND a.businessId = b.businessId
        order by createdAt ASC limit 1)
        < (1000 * strftime('%s', datetime('now', :defaulterSince))) )
        ))
        as totalCustomers

        From CustomerWithTransactionsInfo a Limit 1
        """
    )
    fun getDefaultersDataForBanner(
        businessId: String,
        defaulterSince: String,
    ): Flow<BulkReminderDbInfo>

    @Query(
        """
        SELECT id, businessId, description, profileImage, balance, lastPayment, lastReminderSendTime, reminderMode,

        (Case when lastPayment > 0 then lastPayment else
        (select createdAt from `transaction` b where a.id == b.customerId AND a.businessId = b.businessId order by createdAt ASC limit 1)
        end) as dueSinceTime

        From CustomerWithTransactionsInfo a
        where balance < 0 AND mobile IS NOT NULL AND businessId =:businessId
        AND dueSinceTime < datetime('now', -:defaulterSince)
        AND (lastReminderSendTime IS NULL OR lastReminderSendTime <= datetime('now', '-1 day'))
        ORDER BY lastReminderSendTime DESC, dueSinceTime Asc, balance DESC
        """
    )
    fun getDefaultersForPendingReminders(
        businessId: String,
        defaulterSince: String,
    ): Flow<List<DbReminderProfile>>

    @Query(
        """
        SELECT id, businessId, description, profileImage, balance, lastPayment, lastReminderSendTime, reminderMode,

        (Case when lastPayment > 0 then lastPayment else
        (select createdAt from `transaction` b
        where a.id == b.customerId AND a.businessId = b.businessId order by createdAt ASC limit 1)
        end) as dueSinceTime

        From CustomerWithTransactionsInfo a
        where balance < 0 AND mobile IS NOT NULL AND businessId =:businessId
        AND dueSinceTime < datetime('now', -:defaulterSince)
        AND (lastReminderSendTime IS NOT NULL OR lastReminderSendTime >= datetime('now', '-1 day'))
        ORDER BY lastReminderSendTime DESC, dueSinceTime Asc, balance DESC
        """
    )
    fun getDefaultersForTodaysReminders(
        businessId: String,
        defaulterSince: String,
    ): Flow<List<DbReminderProfile>>

    @Query("""UPDATE customer SET lastReminderSendTime = :lastReminderSentTime WHERE id = :customerId AND businessId =:businessId""")
    suspend fun updateLastReminderSentTime(businessId: String, customerId: String, lastReminderSentTime: DateTime)

    @Query("""Select id as customerId, lastReminderSendTime from CustomerWithTransactionsInfo where businessId =:businessId AND id in (:customerIds)""")
    suspend fun getDirtyLastReminderSendTime(
        customerIds: List<String>,
        businessId: String,
    ): List<BackendLastReminderSendTime>

    @Query("SELECT state FROM Customer WHERE businessId = :businessId AND id = :customerId")
    suspend fun getState(businessId: String, customerId: String): Int

    @Query("SELECT addTransactionRestricted FROM Customer WHERE businessId = :businessId AND id = :customerId")
    suspend fun getIsAddTransactionRestricted(businessId: String, customerId: String): Boolean
}
