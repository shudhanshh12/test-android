package tech.okcredit.sdk.store

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.sdk.store.database.DBBill
import tech.okcredit.sdk.store.database.DbBillDoc
import tech.okcredit.sdk.store.database.LocalBill

interface BillLocalSource {

    fun areBillsPresent(accountId: String): Observable<Boolean>

    fun getAllBillsForAccount(
        accountId: String,
        startTimeInMs: String,
        endTimeInMs: String,
        businessId: String
    ): Observable<List<LocalBill>>

    fun getStartTime(businessId: String): Single<Long>

    fun putBills(dbBillList: MutableList<DBBill>, dbDocList: MutableList<DbBillDoc>): Completable

    fun clearAllTables(): Completable

    fun deleteBill(billId: String): Completable

    fun getBill(billId: String, businessId: String): Observable<LocalBill>

    fun updateSeenTime(accountId: String, currentTimestamp: String, businessId: String): Completable

    fun getUnseenBillCount(accountId: String, startTime: Long, businessId: String): Observable<Int>

    fun putNewBillDocsInDb(localBillDocList: List<DbBillDoc>): Completable

    fun updateNote(note: String, billId: String): Completable

    fun deleteBillDoc(id: String): Completable

    fun getLastSeenTime(accountId: String): Single<String>

    fun getUnreadBillCounts(businessId: String): Observable<Map<String, Int>>

    fun getUnreadBillCount(accountId: String, businessId: String): Observable<Int>

    fun getTotalBillCount(accountId: String, businessId: String): Observable<Int>

    fun setBillAdoptionTime(millis: Long, businessId: String): Completable

    fun getBillAdoptionTime(defaultTime: Long, businessId: String): Observable<Long>

    fun insertOrUpdateBills(bill: DBBill, docList: List<DbBillDoc>?)
}
