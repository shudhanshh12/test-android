package `in`.okcredit.merchant.suppliercredit.store.database

import `in`.okcredit.merchant.suppliercredit.model.NotificationReminderData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import merchant.okcredit.suppliercredit.contract.FlyweightSupplier
import org.joda.time.DateTime

@Dao
interface SupplierDataBaseDao {
    /*********************** Suppliers ***********************/
    @Query("SELECT count(*) FROM SupplierWithTransactionsInfo WHERE id = :supplierId")
    fun supplierExists(supplierId: String): Int

    @Query("SELECT * FROM SupplierWithTransactionsInfo WHERE id = :supplierId")
    fun getSupplier(supplierId: String): Flowable<SupplierWithTransactionsInfo>

    @Query("SELECT * FROM SupplierWithTransactionsInfo WHERE businessId = :businessId")
    fun getSuppliers(businessId: String): Flowable<List<SupplierWithTransactionsInfo>>

    @Query("SELECT * FROM SupplierWithTransactionsInfo WHERE businessId = :businessId AND mobile = :mobile")
    fun getSupplierByMobile(mobile: String, businessId: String): Single<SupplierWithTransactionsInfo>

    @Query("SELECT state FROM supplier WHERE businessId = :businessId AND id = :supplierId")
    suspend fun getState(businessId: String, supplierId: String): Int

    @Query("SELECT addTransactionRestricted FROM supplier WHERE businessId = :businessId AND id = :supplierId")
    suspend fun getIsAddTransactionRestricted(businessId: String, supplierId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSupplier(vararg supplier: Supplier)

    @Query("SELECT * FROM SupplierWithTransactionsInfo WHERE businessId = :businessId ORDER BY lastActivityTime DESC")
    fun listSuppliers(businessId: String): Flowable<List<SupplierWithTransactionsInfo>>

    @Query("SELECT * FROM SupplierWithTransactionsInfo WHERE businessId = :businessId AND deleted = 0 ORDER BY lastActivityTime DESC")
    fun listActiveSuppliers(businessId: String): Flowable<List<Supplier>>

    @Query("SELECT id as supplierId, name as supplierName FROM SupplierWithTransactionsInfo WHERE businessId = :businessId AND deleted = 0 ORDER BY lastActivityTime DESC")
    fun listActiveSuppliersByFlyweight(businessId: String): Flow<List<FlyweightSupplier>>

    @Query("SELECT id FROM SupplierWithTransactionsInfo WHERE deleted = 0 AND businessId = :businessId ORDER BY lastActivityTime DESC")
    fun listActiveSuppliersIds(businessId: String): Flowable<List<String>>

    @Query("SELECT count(*) FROM Supplier WHERE businessId = :businessId")
    fun getSuppliersCount(businessId: String): Observable<Long>

    @Query("DELETE FROM Supplier WHERE businessId = :businessId")
    fun deleteSuppliers(businessId: String)

    @Query("Select count(*) From SupplierWithTransactionsInfo WHERE deleted = 0 AND businessId = :businessId")
    fun getActiveSuppliersCount(businessId: String): Flow<Long>

    @Query("DELETE FROM Supplier")
    fun deleteAllSuppliers()

    @Query("SELECT SUM(balance) from Supplier where businessId = :businessId and state is not 3")
    fun getSupplierBalance(businessId: String): Observable<Long>

//    @Query("UPDATE `TRANSACTION` SET id =:serverTransactionId ,syncing=:synced WHERE id = :localTransactionId")
//    fun replaceTransaction(localTransactionId: String, serverTransactionId: String, synced: Boolean)

    @Query("DELETE FROM  `TRANSACTION` WHERE id=:txnId")
    fun removeTransaction(txnId: String)

    @Query("UPDATE Supplier SET lastViewTime = :time WHERE id = :supplierId")
    fun updateLastViewTime(supplierId: String, time: Long)

    @Query("UPDATE Supplier SET lastActivityTime = :time WHERE id = :supplierId")
    fun updateLastActivityTime(supplierId: String, time: Long)

    /*********************** Transactions ***********************/

    @Query("SELECT * FROM `TRANSACTION` WHERE supplierId = :supplierId  AND createTime > :txnStartTime AND businessId = :businessId ORDER BY createTime DESC")
    fun listTransactions(supplierId: String, txnStartTime: Long, businessId: String): Flowable<List<Transaction>>

    @Query("SELECT * FROM `TRANSACTION` WHERE supplierId = :supplierId  AND createTime > :txnStartTime AND businessId = :businessId ORDER BY billDate DESC")
    fun listTransactionsSortedByBillDate(
        supplierId: String,
        txnStartTime: Long,
        businessId: String,
    ): Flowable<List<Transaction>>

    @Query("SELECT * FROM `TRANSACTION` WHERE supplierId = :supplierId AND businessId = :businessId ORDER BY createTime DESC")
    fun listTransactions(supplierId: String, businessId: String): Flowable<List<Transaction>>

    @Query("SELECT * FROM `TRANSACTION` WHERE businessId = :businessId AND syncing = 0 ORDER BY updateTime ASC")
    fun listDirtyTransactions(businessId: String): Flowable<List<Transaction>>

    @Query("SELECT * FROM `TRANSACTION` WHERE id = :txnId")
    fun geTransaction(txnId: String): Flowable<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveTransaction(vararg transaction: Transaction)

    @Query("DELETE FROM  `TRANSACTION` WHERE supplierId = :supplierId")
    fun removeAllTransaction(supplierId: String)

    @Query("DELETE FROM `TRANSACTION` WHERE businessId = :businessId")
    fun clearTransactions(businessId: String)

    @Query("DELETE FROM `TRANSACTION`")
    fun deleteAllTransactions()

    @androidx.room.Transaction
    fun resetSupplierList(businessId: String, vararg list: Supplier) {
        deleteSuppliers(businessId)
        saveSupplier(*list)
    }

    @Query("UPDATE Supplier SET name = :updatedName WHERE id = :supplierId")
    fun updateSupplierName(updatedName: String, supplierId: String)

    @Query("SELECT * FROM `TRANSACTION` WHERE supplierId = :supplierId AND createTime > :customerTxnTime and billDate > :startTime AND billDate <= :endTime AND deleted == 0 AND businessId = :businessId ORDER BY billDate DESC")
    fun listCustomerActiveTransactionsBetweenBillDate(
        supplierId: String,
        customerTxnTime: Long,
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>>

    @Query("SELECT * FROM `TRANSACTION` WHERE businessId = :businessId AND billDate > :startTime AND billDate <= :endTime AND deleted == 0 ORDER BY billDate DESC")
    fun listActiveTransactionsBetweenBillDate(
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Flowable<List<Transaction?>?>?

    @Query("SELECT createTime FROM `TRANSACTION` where supplierId =:supplierId and businessId =:businessId ORDER BY createTime DESC LIMIT 1")
    suspend fun getLatestTransactionCreateTime(supplierId: String, businessId: String): DateTime

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNotificationReminder(notificationReminder: List<NotificationReminder>): Completable

    @Query("SELECT MAX(createdAt) FROM NOTIFICATIONREMINDER WHERE businessId = :businessId ")
    fun getNotificationReminderStartTime(businessId: String): Single<String>

    @Query(
        """
        SELECT
                nr3.createdAt,
                nr3.name,
                nr3.profileImage,
                nr3.balance,
                t.amount as lastPayment,
                Max(t.createTime) as lastPaymentDate,
                nr3.accountId,
                nr3.id as notificationId
        FROM (
                SELECT

                    min(nr2.createdAt) createdAt ,
                    nr2.accountId, nr2.createdAt ,
                    sti.name,
                    sti.profileImage,
                    sti.balance,
                    nr2.id
                        FROM
                            (SELECT * FROM
                                NotificationReminder  nr1
                                    WHERE   CAST(nr1.expiresAt   AS INTEGER)   > :currentTime
                                    and nr1.status = -1
                                    and nr1.businessId = :businessId ) nr2
                                    INNER JOIN
                                    SupplierWithTransactionsInfo  sti
                                    On  nr2.accountId= sti.id
                                    and  CAST(nr2.createdAt   AS INTEGER )  >   CAST (sti.lastActivityTime AS INTEGER)
                                    and sti.balance < 0  and sti.deleted = 0
                                    group by  nr2.id
                                    ) nr3
                  left join  [Transaction]   t
                  on  nr3.accountId= t.supplierId and t.payment =1 and deleted =0
                  group by  nr3.id
                  order by nr3.createdAt asc
    """
    )
    fun getNotificationReminders(currentTime: Long, businessId: String): Single<List<NotificationReminderData>>

    @Query(
        """
            SELECT * FROM
            NOTIFICATIONREMINDER
            WHERE status != :statusNoAction
            and businessId = :businessId
        """
    )
    fun getNotificationRemindersWithAction(statusNoAction: Int, businessId: String): Single<List<NotificationReminder>>

    @Delete
    fun deletedSyncedReminders(notificationReminder: List<NotificationReminder>): Completable

    @Query(" UPDATE NOTIFICATIONREMINDER SET status = :status where id= :notificationId")
    fun updateNotificationReminder(notificationId: String, status: Int): Completable
}
