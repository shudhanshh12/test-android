package `in`.okcredit.merchant.customer_ui.data.local

import `in`.okcredit.merchant.customer_ui.data.local.db.CollectionTriggeredCustomers
import `in`.okcredit.merchant.customer_ui.data.local.db.CustomerAdditionalInfo
import `in`.okcredit.merchant.customer_ui.data.local.db.CustomerDatabaseDao
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class CustomerLocalDataSource @Inject constructor(
    private val customerUiPreferences: Lazy<CustomerUiPreferences>,
    private val customerDatabaseDao: Lazy<CustomerDatabaseDao>,
) {

    suspend fun setTxnCntForCollectionNudgeOnCustomerScr(count: Int, businessId: String) {
        customerUiPreferences.get().setTxnCntForCollectionNudgeOnCustomerScr(count, businessId)
    }

    fun getTxnCntForCollectionNudgeOnCustomerScr(businessId: String): Int {
        return customerUiPreferences.get().getTxnCntForCollectionNudgeOnCustomerScr(businessId)
    }

    suspend fun setTxnCntForCollectionNudgeOnSetDueDate(count: Int, businessId: String) {
        customerUiPreferences.get().setTxnCntForCollectionNudgeOnSetDueDate(count, businessId)
    }

    fun getTxnCntForCollectionNudgeOnSetDueDate(businessId: String): Int {
        return customerUiPreferences.get().getTxnCntForCollectionNudgeOnSetDueDate(businessId)
    }

    suspend fun setTxnCntForCollectionNudgeOnDueDateCrossed(count: Int, businessId: String) {
        customerUiPreferences.get().setTxnCntForCollectionNudgeOnDueDateCrossed(count, businessId)
    }

    fun getTxnCntForCollectionNudgeOnDueDateCrossed(businessId: String): Int {
        return customerUiPreferences.get().getTxnCntForCollectionNudgeOnDueDateCrossed(businessId)
    }

    fun canShowAddNoteTutorial() = customerUiPreferences.get().canShowAddNoteTutorial()

    fun setAddBillShowed() = customerUiPreferences.get().setAddBillShowed()

    fun isAddBillTooltipShowed() = customerUiPreferences.get().isAddBillTooltipShowed()

    fun clearRoboflowAddBillToolTipPref() = customerUiPreferences.get().clearRoboflowAddBillToolTipPref()

    suspend fun setShowCalculatorEducation(show: Boolean) {
        customerUiPreferences.get().setShowCalculatorEducation(show)
    }

    fun canShowCalculatorEducation() = customerUiPreferences.get().canShowCalculatorEducation()

    fun getTxnCntForCalculatorEducation(): Int = customerUiPreferences.get().getTxnCntForCalculatorEducation()

    fun setTxnCountForCalculatorEducation(txnCount: Int) {
        customerUiPreferences.get().setTxnCountForCalculatorEducation(txnCount)
    }

    suspend fun setCustomerWithCollectionContextualMessage(customerId: String, txnId: String, businessId: String) {
        val updatedRows = customerDatabaseDao.get().enableCollectionTrigger(customerId, txnId)
        if (updatedRows == 0) {
            customerDatabaseDao.get().insertCustomerAdditionalInfo(
                CustomerAdditionalInfo(
                    customer_id = customerId,
                    collectionContextualEnabled = true,
                    txnIdForCollectionTrigger = txnId,
                    businessId = businessId
                )
            )
        }
    }

    suspend fun getCustomerWithCollectionContextualMessage(businessId: String): List<CollectionTriggeredCustomers> {
        return customerDatabaseDao.get().findCustomersWithContextualTrigger(businessId)
    }

    suspend fun setTxnCountForPaymentIntent(customerId: String, txnCount: Int, businessId: String) {
        val updatedRows = customerDatabaseDao.get().setTxnCountForPaymentIntent(customerId, txnCount)
        if (updatedRows == 0) {
            customerDatabaseDao.get().insertCustomerAdditionalInfo(
                CustomerAdditionalInfo(
                    customer_id = customerId,
                    txnCountOnPaymentIntentTrigger = txnCount,
                    businessId = businessId
                )
            )
        }
    }

    suspend fun getTxnCountForPaymentIntentEnabled(customerId: String): Int? {
        return customerDatabaseDao.get().getTxnCountForPaymentIntentEnabled(customerId)
    }

    fun setLastContextualTriggerTimestamp(timestamp: Long) {
        customerUiPreferences.get().setLastContextualTriggerTimestamp(timestamp)
    }

    fun getLastContextualTriggerTimestamp() = customerUiPreferences.get().getLastContextualTriggerTimestamp()

    fun clearLocalData(): Completable {
        return rxCompletable {
            customerUiPreferences.get().clear()
            customerDatabaseDao.get().deleteAllInfo()
        }.subscribeOn(Schedulers.io())
    }

    suspend fun staffLinkEducationShown(): Boolean {
        return customerUiPreferences.get().showStaffLinkEducation()
    }

    suspend fun setStaffLinkEducation(show: Boolean) {
        customerUiPreferences.get().setStaffLinkEducation(show)
    }

    suspend fun getCustomerIdsForSyncLastReminderSendTime(businessId: String): Set<String> {
        return customerUiPreferences.get().getSavedCustomerIdsForSyncLastReminderSendTime(businessId)
    }

    suspend fun saveCustomerIdForSyncLastReminderSentTime(businessId: String, updatedList: Set<String>) {
        return customerUiPreferences.get().saveCustomerIdForSyncLastReminderSentTime(businessId, updatedList)
    }

    suspend fun clearDirtyLastReminderSendTimeCustomerIds(businessId: String) {
        return customerUiPreferences.get().clearDirtyLastReminderSendTimeCustomerIds(businessId)
    }

    suspend fun setExpandedQrShownCount(count: Int) {
        customerUiPreferences.get().setExpandedQrShownCount(count)
    }

    suspend fun getExpandedQrShownCount(): Int {
        return customerUiPreferences.get().getExpandedQrShownCount()
    }
}
