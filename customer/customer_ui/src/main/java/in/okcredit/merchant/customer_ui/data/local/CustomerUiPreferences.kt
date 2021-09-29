package `in`.okcredit.merchant.customer_ui.data.local

import `in`.okcredit.backend.contract.RxSharedPrefValues.SHOULD_SHOW_NOTE_TUTORIAL
import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerScreenSortSelection.CustomerScreenSortSelection
import android.content.Context
import com.google.gson.Gson
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.extensions.fromJson
import tech.okcredit.android.base.preferences.*
import javax.inject.Inject

class CustomerUiPreferences @Inject constructor(
    context: Context,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    migrations: Lazy<Migrations>,
) : OkcSharedPreferences(
    context = context,
    prefName = SHARED_PREF_NAME,
    version = VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {

    companion object {
        const val VERSION = 1
        private const val SHARED_PREF_NAME = "customer_ui_prefs"

        private const val PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT = "collection_nudge_txn_count"
        private const val PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT_FOR_SET_DUE_DATE =
            "collection_nudge_txn_count_for_set_due_date"
        private const val PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT_FOR_DUE_DATE_CROSSED =
            "collection_nudge_txn_count_for_due_date_crossed"

        private const val ROBOFLOW_ADD_BILLS_TOOLTIP = "roboflow_add_bills_tooltip"
        private const val SHOW_CALCULATOR_EDUCATION = "show_calculator_education"
        private const val TXN_COUNT_FOR_CALCULATOR_EDUCATION = "txn_count_calculator_education"
        private const val CUSTOMER_COLLECTION_CONTEXTUAL_TRIGGER = "customer_collection_contextual_trigger"
        private const val SHOW_COLLECTION_STAFF_LINK_EDUCATION = "show_collection_staff_link_education"
        private const val DIRTY_LAST_REMINDER_SEND_TIME_LIST = "dirty_last_reminder_send_time"
        private const val PREF_BUSINESS_CUSTOMER_SCREEN_SORT = "customer_screen_sort"

        private const val PREF_ADD_PAYMENT_EXPANDED_QR_SHOWN_COUNT = "add_payment_expanded_qr_shown_count"
    }

    private val gson = Gson()

    class Migrations @Inject constructor(
        private val getBusinessIdList: Lazy<GetBusinessIdList>,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { prefs ->
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(
                prefs,
                listOf(
                    PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT,
                    PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT_FOR_SET_DUE_DATE,
                    PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT_FOR_DUE_DATE_CROSSED
                ),
                getBusinessIdList.get().execute().first()
            )
        }
    }

    suspend fun setTxnCntForCollectionNudgeOnCustomerScr(count: Int, businessId: String) {
        set(PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT, count, Scope.Business(businessId))
    }

    fun getTxnCntForCollectionNudgeOnCustomerScr(businessId: String): Int {
        return blockingGetInt(PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT, Scope.Business(businessId))
    }

    suspend fun setTxnCntForCollectionNudgeOnSetDueDate(count: Int, businessId: String) {
        set(PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT_FOR_SET_DUE_DATE, count, Scope.Business(businessId))
    }

    fun getTxnCntForCollectionNudgeOnSetDueDate(businessId: String): Int {
        return blockingGetInt(PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT_FOR_SET_DUE_DATE, Scope.Business(businessId))
    }

    suspend fun setTxnCntForCollectionNudgeOnDueDateCrossed(count: Int, businessId: String) {
        set(PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT_FOR_DUE_DATE_CROSSED, count, Scope.Business(businessId))
    }

    fun getTxnCntForCollectionNudgeOnDueDateCrossed(businessId: String): Int {
        return blockingGetInt(PREF_BUSINESS_COLLECTION_NUDGE_TXN_COUNT_FOR_DUE_DATE_CROSSED, Scope.Business(businessId))
    }

    fun canShowAddNoteTutorial() = getBoolean(SHOULD_SHOW_NOTE_TUTORIAL, Scope.Individual)

    fun isAddBillTooltipShowed() = getBoolean(ROBOFLOW_ADD_BILLS_TOOLTIP, Scope.Individual)

    fun setAddBillShowed() = blockingSet(ROBOFLOW_ADD_BILLS_TOOLTIP, true, Scope.Individual)

    fun clearRoboflowAddBillToolTipPref() = blockingRemove(ROBOFLOW_ADD_BILLS_TOOLTIP, Scope.Individual)

    suspend fun setShowCalculatorEducation(show: Boolean) = set(SHOW_CALCULATOR_EDUCATION, show, Scope.Individual)

    fun canShowCalculatorEducation() = getBoolean(SHOW_CALCULATOR_EDUCATION, Scope.Individual, true).asObservable()

    fun getTxnCntForCalculatorEducation() = blockingGetInt(TXN_COUNT_FOR_CALCULATOR_EDUCATION, Scope.Individual, -1)

    fun setTxnCountForCalculatorEducation(txnCount: Int) {
        blockingSet(TXN_COUNT_FOR_CALCULATOR_EDUCATION, txnCount, Scope.Individual)
    }

    fun setLastContextualTriggerTimestamp(timestamp: Long) {
        blockingSet(CUSTOMER_COLLECTION_CONTEXTUAL_TRIGGER, timestamp, Scope.Individual)
    }

    fun getLastContextualTriggerTimestamp() = getLong(CUSTOMER_COLLECTION_CONTEXTUAL_TRIGGER, Scope.Individual)

    suspend fun setStaffLinkEducation(show: Boolean) {
        set(SHOW_COLLECTION_STAFF_LINK_EDUCATION, show, Scope.Individual)
    }

    suspend fun showStaffLinkEducation() = getBoolean(SHOW_COLLECTION_STAFF_LINK_EDUCATION, Scope.Individual).first()

    suspend fun getSavedCustomerIdsForSyncLastReminderSendTime(businessId: String) = withContext(Dispatchers.IO) {
        val json = getString(DIRTY_LAST_REMINDER_SEND_TIME_LIST, Scope.Business(businessId)).first()
        gson.fromJson<Set<String>>(json) ?: emptySet()
    }

    suspend fun saveCustomerIdForSyncLastReminderSentTime(businessId: String, updatedList: Set<String>) {
        set(DIRTY_LAST_REMINDER_SEND_TIME_LIST, gson.toJson(updatedList), Scope.Business(businessId))
    }

    suspend fun clearDirtyLastReminderSendTimeCustomerIds(businessId: String) {
        withContext(Dispatchers.IO) {
            remove(DIRTY_LAST_REMINDER_SEND_TIME_LIST, Scope.Business(businessId))
        }
    }

    suspend fun setExpandedQrShownCount(count: Int) {
        withContext(Dispatchers.IO) {
            set(PREF_ADD_PAYMENT_EXPANDED_QR_SHOWN_COUNT, count, Scope.Individual)
        }
    }

    suspend fun getExpandedQrShownCount() = withContext(Dispatchers.IO) {
        getInt(PREF_ADD_PAYMENT_EXPANDED_QR_SHOWN_COUNT, Scope.Individual).first()
    }

    suspend fun setCustomerScreenSortSelection(customerScreenSortSelection: CustomerScreenSortSelection, businessId: String) {
        set(PREF_BUSINESS_CUSTOMER_SCREEN_SORT, customerScreenSortSelection.value, Scope.Business(businessId))
    }

    suspend fun getCustomerScreenSortSelection(businessId: String): CustomerScreenSortSelection {
        val sortBy = getString(PREF_BUSINESS_CUSTOMER_SCREEN_SORT, Scope.Business(businessId), CustomerScreenSortSelection.BILL_DATE.value)
            .first()
        return CustomerScreenSortSelection.convertToSortBy(sortBy)
    }
}
