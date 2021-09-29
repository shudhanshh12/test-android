package `in`.okcredit.customer.contract

import io.reactivex.Completable
import io.reactivex.Observable

interface CustomerRepository {
    suspend fun setTxnCntForCollectionNudgeOnCustomerScr(count: Int, businessId: String)

    fun getTxnCntForCollectionNudgeOnCustomerScr(businessId: String): Int

    suspend fun setTxnCntForCollectionNudgeOnSetDueDate(count: Int, businessId: String)

    fun getTxnCntForCollectionNudgeOnSetDueDate(businessId: String): Int

    suspend fun setTxnCntForCollectionNudgeOnDueDateCrossed(count: Int, businessId: String)

    fun getTxnCntForCollectionNudgeOnDueDateCrossed(businessId: String): Int

    fun clearRoboflowAddBillToolTipPref()

    suspend fun setShowCalculatorEducation(show: Boolean)

    fun canShowCalculatorEducation(): Observable<Boolean>

    fun clearLocalData(): Completable

    suspend fun saveCustomerIdForSyncLastReminderSentTime(businessId: String, customerId: String)

    suspend fun getCustomerIdForSyncLastReminderSentTime(businessId: String): Set<String>

    suspend fun clearDirtyLastReminderSendTimeCustomerIds(businessId: String)
}
