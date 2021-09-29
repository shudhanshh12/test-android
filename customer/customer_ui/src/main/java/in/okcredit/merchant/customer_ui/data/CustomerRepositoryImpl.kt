package `in`.okcredit.merchant.customer_ui.data

import `in`.okcredit.customer.contract.CustomerRepository
import `in`.okcredit.merchant.customer_ui.data.local.CustomerLocalDataSource
import `in`.okcredit.merchant.customer_ui.data.server.CustomerRemoteDataSource
import `in`.okcredit.merchant.customer_ui.data.server.model.request.*
import `in`.okcredit.merchant.customer_ui.data.server.model.response.ActiveStaffLinkResponse
import `in`.okcredit.merchant.customer_ui.data.server.model.response.CreateStaffLinkResponse
import `in`.okcredit.merchant.customer_ui.data.server.model.response.GooglePayPaymentResponse
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import dagger.Lazy
import io.reactivex.Completable
import okhttp3.ResponseBody
import tech.okcredit.base.network.ApiError
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val localDataSource: Lazy<CustomerLocalDataSource>,
    private val remoteDataSource: Lazy<CustomerRemoteDataSource>,
) : CustomerRepository {

    fun canShowAddNoteTutorial() = localDataSource.get().canShowAddNoteTutorial()

    fun setAddBillToolTipShowed() = localDataSource.get().setAddBillShowed()

    fun isAddBillTooltipShowed() = localDataSource.get().isAddBillTooltipShowed()

    @Throws(ApiError::class)
    suspend fun initiateGooglePayPayment(
        amount: Long,
        mobile: String,
        transactionId: String,
        linkId: String,
        customerId: String,
        businessName: String,
        businessId: String,
    ): GooglePayPaymentResponse {
        return remoteDataSource.get().initiateGooglePayPayment(
            GooglePayPaymentRequest(
                amount = amount,
                customer_mobile_number = mobile,
                merchant_name = businessName,
                transactionId = transactionId,
                linkId = linkId,
                customerId = customerId,
            ),
            businessId = businessId
        )
    }

    suspend fun listSubscriptions(customerId: String?, businessId: String): List<Subscription> {
        return remoteDataSource.get()
            .getSubscriptionList(MerchantRequest(businessId, customerId), businessId).subscriptions
    }

    suspend fun getSubscription(subscriptionId: String, businessId: String): Subscription {
        return remoteDataSource.get().getSubscription(GetSubscriptionRequest(subscriptionId), businessId).subscription
    }

    suspend fun addSubscription(
        customerId: String,
        amount: Long,
        name: String,
        frequency: SubscriptionFrequency,
        startDate: Long? = null,
        days: List<Int>? = null,
        businessId: String,
    ): Subscription {
        val request = AddSubscriptionRequest(
            account_id = customerId,
            frequency = frequency.value,
            startDate = startDate,
            name = name,
            week = days,
            amount = amount
        )
        return remoteDataSource.get().addSubscription(addSubscriptionRequest = request, businessId = businessId)
    }

    suspend fun deleteSubscription(
        updatedSubscription: Subscription,
        businessId: String,
    ): ResponseBody {
        return remoteDataSource.get().updateSubscription(
            updatedSubscription, businessId
        )
    }

    override suspend fun setTxnCntForCollectionNudgeOnCustomerScr(count: Int, businessId: String) {
        localDataSource.get().setTxnCntForCollectionNudgeOnCustomerScr(count, businessId)
    }

    override fun getTxnCntForCollectionNudgeOnCustomerScr(businessId: String): Int {
        return localDataSource.get().getTxnCntForCollectionNudgeOnCustomerScr(businessId)
    }

    override suspend fun setTxnCntForCollectionNudgeOnSetDueDate(count: Int, businessId: String) {
        localDataSource.get().setTxnCntForCollectionNudgeOnSetDueDate(count, businessId)
    }

    override fun getTxnCntForCollectionNudgeOnSetDueDate(businessId: String): Int {
        return localDataSource.get().getTxnCntForCollectionNudgeOnSetDueDate(businessId)
    }

    override suspend fun setTxnCntForCollectionNudgeOnDueDateCrossed(count: Int, businessId: String) {
        localDataSource.get().setTxnCntForCollectionNudgeOnDueDateCrossed(count, businessId)
    }

    override fun getTxnCntForCollectionNudgeOnDueDateCrossed(businessId: String): Int {
        return localDataSource.get().getTxnCntForCollectionNudgeOnDueDateCrossed(businessId)
    }

    override fun clearRoboflowAddBillToolTipPref() = localDataSource.get().clearRoboflowAddBillToolTipPref()

    override suspend fun setShowCalculatorEducation(show: Boolean) {
        localDataSource.get().setShowCalculatorEducation(show)
    }

    override fun canShowCalculatorEducation() = localDataSource.get().canShowCalculatorEducation()

    override fun clearLocalData(): Completable {
        return localDataSource.get().clearLocalData()
    }

    fun getTxnCntForCalculatorEducation() = localDataSource.get().getTxnCntForCalculatorEducation()

    fun setTxnCountForCalculatorEducation(txnCount: Int) =
        localDataSource.get().setTxnCountForCalculatorEducation(txnCount)

    suspend fun getCustomerWithCollectionContextualMessage(businessId: String) =
        localDataSource.get().getCustomerWithCollectionContextualMessage(businessId)

    suspend fun setCustomerWithCollectionContextualMessage(customerId: String, txnId: String, businessId: String) {
        localDataSource.get().setCustomerWithCollectionContextualMessage(customerId, txnId, businessId)
    }

    suspend fun setTxnCountForPaymentIntent(customerId: String, txnCount: Int, businessId: String) {
        localDataSource.get().setTxnCountForPaymentIntent(customerId, txnCount, businessId)
    }

    suspend fun getTxnCountForPaymentIntentEnabled(customerId: String): Int? {
        return localDataSource.get().getTxnCountForPaymentIntentEnabled(customerId)
    }

    fun setLastContextualTriggerTimestamp(timestamp: Long) {
        localDataSource.get().setLastContextualTriggerTimestamp(timestamp)
    }

    fun getLastContextualTriggerTimestamp() = localDataSource.get().getLastContextualTriggerTimestamp()

    suspend fun createCustomerStaffLink(customerIds: List<String>, businessId: String): CreateStaffLinkResponse {
        return remoteDataSource.get().createCustomerStaffLink(CreateStaffLinkRequest(customerIds), businessId)
    }

    suspend fun activeStaffLinkDetails(businessId: String): ActiveStaffLinkResponse {
        return remoteDataSource.get().activeStaffLinkDetails(businessId)
    }

    suspend fun deleteCollectionStaffLink(businessId: String) {
        remoteDataSource.get().deleteCollectionStaffLink(businessId)
    }

    suspend fun editCollectionStaffLink(
        linkId: String,
        customerIds: List<String>,
        action: EditAction,
        businessId: String,
    ) {
        remoteDataSource.get().editCollectionStaffLink(
            EditStaffLinkRequest(
                linkId = linkId,
                accountIds = customerIds,
                action = action.value
            ),
            businessId
        )
    }

    suspend fun staffLinkEducationShown(): Boolean {
        return localDataSource.get().staffLinkEducationShown()
    }

    suspend fun setStaffLinkEducation(show: Boolean) {
        localDataSource.get().setStaffLinkEducation(show)
    }

    suspend fun setExpandedQrShownCount(count: Int) {
        localDataSource.get().setExpandedQrShownCount(count)
    }

    suspend fun getExpandedQrShownCount(): Int {
        return localDataSource.get().getExpandedQrShownCount()
    }

    override suspend fun saveCustomerIdForSyncLastReminderSentTime(businessId: String, customerId: String) {
        val getUpdatedList = localDataSource.get().getCustomerIdsForSyncLastReminderSendTime(businessId)
            .toMutableSet()
            .apply { add(customerId) }

        localDataSource.get().saveCustomerIdForSyncLastReminderSentTime(businessId, getUpdatedList)
    }

    override suspend fun getCustomerIdForSyncLastReminderSentTime(businessId: String): Set<String> {
        return localDataSource.get().getCustomerIdsForSyncLastReminderSendTime(businessId)
    }

    override suspend fun clearDirtyLastReminderSendTimeCustomerIds(businessId: String) {
        return localDataSource.get().clearDirtyLastReminderSendTimeCustomerIds(businessId = businessId)
    }
}
