package tech.okcredit.bills

import io.reactivex.Completable
import io.reactivex.Observable

interface BillRepository {

    fun scheduleBillSync(businessId: String): Completable

    fun clearLocalData(): Completable

    fun getUnreadBillCounts(businessId: String): Observable<Map<String, Int>>

    fun getUnreadBillCount(accountId: String, businessId: String): Observable<Int>

    fun getTotalBillCount(accountId: String, businessId: String): Observable<Int>

    // Feature adoption time is used to figure out unread bill count.
    // All the bills which are added after feature adoption time will be considered as unread
    // Without adoption time on login all the bills are considered as unread since there's no last seen time
    fun setBillAdoptionTime(): Completable

    fun resetBillAdoptionTime(businessId: String): Completable
}
