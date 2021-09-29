package tech.okcredit.sdk.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.sdk.models.BillWithDocs

@Dao
interface BillDatabaseDao {

    @Query("select exists (select *  from DBBill where accountId =:accountId and businessId = :businessId limit 1)")
    fun areBillsPresent(accountId: String, businessId: String): Observable<Boolean>

    @Query("select * from DBBill where accountId =:accountId and (billDate between :startTimeInMs and :endTimeInMs) and deleted = 0 and businessId = :businessId order by  billDate desc")
    @Transaction
    fun getAllBillsForAccount(
        accountId: String,
        startTimeInMs: String,
        endTimeInMs: String,
        businessId: String
    ): Observable<List<BillWithDocs>>

    @Query("select max (updatedAt) from DBBill where businessId = :businessId")
    fun getStartTime(businessId: String): Single<Long>

    @androidx.room.Transaction
    fun createTransaction(dbBillList: MutableList<DBBill>, dbDocList: MutableList<DbBillDoc>) {
        insertBill(dbBillList)
        insertDoc(dbDocList)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBill(dbBill: List<DBBill>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDoc(dbBill: List<DbBillDoc>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBill(dbBill: DBBill)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDoc(dbBill: DbBillDoc)

    @Transaction
    @Query("SELECT * FROM DBBill where businessId = :businessId")
    fun getBillsWithBillDocForAccount(businessId: String): Observable<List<BillWithDocs>>

    @androidx.room.Transaction
    fun deleteBill(billId: String) {
        deleteFromBillTable(billId)
        deleteFromBillDocTable(billId)
    }

    @Query("DELETE FROM DBBill WHERE id = :billId")
    fun deleteFromBillDocTable(billId: String)

    @Query("DELETE FROM DbBillDoc WHERE billId = :billId")
    fun deleteFromBillTable(billId: String): Completable

    @Query("select * from DBBill where  id =:billId")
    @Transaction
    fun getBill(billId: String): Observable<BillWithDocs>

    @Query("UPDATE Account SET lastSeen=:currentTimestamp  WHERE accountId =:accountId")
    fun updateSeenTime(accountId: String, currentTimestamp: String): Completable

    @Query("select lastSeen from Account where  accountId =:accountId")
    fun getLastSeenTime(accountId: String): Observable<List<String>>

    fun getDistinctLastSeenTime(accountId: String): Observable<List<String>> =
        getLastSeenTime(accountId).distinctUntilChanged()

    @Query("select count(*) from DBBill where  accountId =:accountId and billDate > :lastSeenTime and deleted = 0 and (createdAt >:startTime) and createdByMe = 0 and businessId = :businessId ")
    fun getUnseenBillCount(accountId: String, lastSeenTime: String, startTime: Long, businessId: String): Observable<List<Int>>

    @Insert
    fun putBillDocs(localBillDocList: List<DbBillDoc>): Completable

    @Query("UPDATE DBBill SET note=:note  WHERE id =:billId")
    fun updateNote(note: String, billId: String): Completable

    @Query("DELETE FROM DbBillDoc WHERE id = :id")
    fun deleteBillDoc(id: String): Completable

    @Query("select exists (select * from Account where accountId =:accountId limit 1)")
    fun isAccountPresent(accountId: String): Boolean

    @Query("insert into Account (accountId, lastSeen, businessId) values  (:accountId, :currentTimestamp, :businessId) ")
    fun insertAccount(accountId: String, currentTimestamp: String, businessId: String): Completable

    @Query(
        """select DBBill.accountId, COUNT(*) as count from DBBill LEFT JOIN Account
                    ON Account.accountId = DBBill.accountId
                    WHERE CAST(ifnull(DBBill.updatedAt, createdAt) as INTEGER)
                    > CAST(ifnull(Account.lastSeen, :billAdoptionTime) as INTEGER)
                    AND deleted = 0 AND createdByMe = 0 AND DBBill.businessId = :businessId GROUP BY DBBill.accountId"""
    )
    fun getUnreadBillCounts(billAdoptionTime: Long, businessId: String): Observable<List<UnreadBillCount>>

    fun getDistinctUnreadBillCounts(billAdoptionTime: Long, businessId: String): Observable<List<UnreadBillCount>> =
        getUnreadBillCounts(billAdoptionTime, businessId).distinctUntilChanged()

    @Query(
        """select COUNT(*) as count from DBBill LEFT JOIN ACCOUNT ON Account.accountId = DBBill.accountId
                WHERE DBBill.accountId = :accountId AND DBBill.businessId = :businessId
                AND CAST(ifnull(DBBill.updatedAt, createdAt) as INTEGER)
                > CAST(ifnull(Account.lastSeen, :billAdoptionTime) as INTEGER)
                AND deleted = 0 AND createdByMe = 0"""
    )
    fun getUnreadBillCount(accountId: String, billAdoptionTime: Long, businessId: String): Observable<Int>

    fun getDistinctUnreadBillCount(accountId: String, billAdoptionTime: Long, businessId: String): Observable<Int> =
        getUnreadBillCount(accountId, billAdoptionTime, businessId).distinctUntilChanged()

    @Query("select COUNT(*) as count from DBBill WHERE DBBill.accountId = :accountId AND DBBill.businessId = :businessId AND deleted = 0")
    fun getTotalBillCount(accountId: String, businessId: String): Observable<Int>

    fun getDistinctTotalBillCount(accountId: String, businessId: String): Observable<Int> =
        getTotalBillCount(accountId, businessId).distinctUntilChanged()

    @Query("DELETE FROM DbBillDoc WHERE billId = :billId AND businessId = :businessId")
    fun deleteAllBillDoc(billId: String, businessId: String)

    @androidx.room.Transaction
    fun insertOrUpdateBills(bill: DBBill, docList: List<DbBillDoc>?) {
        insertBill(bill)
        deleteAllBillDoc(bill.id, bill.businessId)
        docList?.let { insertDoc(it) }
    }
}
