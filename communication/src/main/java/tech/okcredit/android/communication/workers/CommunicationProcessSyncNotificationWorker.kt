package tech.okcredit.android.communication.workers

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.GetBusinessIdList
import android.content.Context
import androidx.work.WorkerParameters
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.asObservable
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.communication.CommunicationRemoteSource
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.CommunicationRepositoryImpl
import tech.okcredit.android.communication.NotificationData
import tech.okcredit.android.communication.analytics.CommunicationTracker
import tech.okcredit.android.communication.workers.CommunicationProcessSyncNotificationWorker.Companion.SyncNotificationType.CONTACT_NETWORK
import tech.okcredit.android.communication.workers.CommunicationProcessSyncNotificationWorker.Companion.SyncNotificationType.INDIVIDUAL
import tech.okcredit.android.communication.workers.CommunicationProcessSyncNotificationWorker.Companion.SyncNotificationType.LOGOUT
import tech.okcredit.android.communication.workers.CommunicationProcessSyncNotificationWorker.Companion.SyncNotificationType.REWARD
import tech.okcredit.base.exceptions.ExceptionUtils
import javax.inject.Inject

class CommunicationProcessSyncNotificationWorker(
    context: Context,
    params: WorkerParameters,
    private val communicationApi: CommunicationRepository,
    private val communicationRemoteSource: CommunicationRemoteSource,
    private val communicationTracker: CommunicationTracker,
    private val getActiveBusinessId: GetActiveBusinessId,
    private val getBusinessIdList: GetBusinessIdList,
) : BaseRxWorker(context, params) {

    companion object {
        const val BUSINESS_ID = "business_id"

        enum class SyncNotificationType(val value: String) {
            SUPPLIER_V2("supplier_v2"),
            SUPPLIER_TXNS("supplier_txns"),
            CUSTOMER_V2("customer_v2"),
            CUSTOMER_TXNS("customer_txns"),
            COLLECTION("collection"),
            MERCHANT("merchant"),
            REWARD("reward"),
            LOGOUT("logout"),
            CUSTOMER_DELETED("customer_deleted"),
            DELETE_CUSTOMER("delete_customer"),
            DUE_INFO("due_info"),
            SYNC_CONTACT("sync_contact"),
            SYNC_ACCOUNTS_FEATURE("sync_account_features"),
            SYNC_PROFILE("sync_profile"),
            RECOVERY_ACTION("recovery_action"),
            CUSTOMER_TXN_RESTRICTION("sync_customer_txn_restriction"),
            SYNC_BILLS("sync_bills"),
            COLLECTION_DESTINATION("collection_destination"),
            COLLECTION_KYC("collection-kyc"),
            PAYMENT_SYNC("payment_sync"),
            ONLINE_COLLETION("merchant_payment"),
            USER_STORIES("sync_okstatus"),
            NOTIFICATION_REMINDER("sync_notification_reminder"),
            INDIVIDUAL("individual"),
            CONTACT_NETWORK("contacts_network"),
        }
    }

    override fun doRxWork(): Completable {
        val notificationData: NotificationData?
        val messageData = inputData.getString(CommunicationRepositoryImpl.MESSAGE_DATA)
        try {
            notificationData = NotificationData.from(messageData!!)
        } catch (e: Exception) {
            ExceptionUtils.logException("Error: NotificationData JSON Parsing", e)
            return Completable.complete()
        }

        if (notificationData.businessId.isNullOrBlank() && !isNotificationForIndividualScope(notificationData.type)) {
            RecordException.recordException(IllegalArgumentException("CommunicationProcessSyncNotificationWorker: type = ${notificationData.businessId}, businessId cannot null or blank"))
            communicationTracker.trackBusinessIdMissing(
                businessId = notificationData.businessId,
                isSyncNotification = true,
                type = notificationData.type,
                campaignId = notificationData.campaignId,
                subCampaignId = notificationData.subCampaignId,
                notificationId = notificationData.notificationId,
            )
        }

        return shouldProcessSync(notificationData.businessId).flatMapCompletable { shouldProcessSync ->
            if (shouldProcessSync) {
                getActiveBusinessId.thisOrActiveBusinessId(notificationData.businessId)
                    .flatMapCompletable { businessId ->
                        communicationApi.executeSyncNotification(notificationData, businessId)
                            .onErrorComplete()
                            .andThen(
                                if (notificationData.notificationId.isNullOrEmpty().not()) {
                                    communicationRemoteSource.acknowledge(businessId, notificationData.notificationId!!)
                                        .doOnError { RecordException.recordException(it) }
                                        .onErrorComplete()
                                } else {
                                    Completable.complete()
                                }
                            )
                    }
            } else {
                Completable.complete() // Ignore notification
            }
        }
    }

    private fun shouldProcessSync(businessId: String?): Single<Boolean> {
        return if (businessId.isNotNullOrBlank()) {
            Single.just(true)
        } else {
            getBusinessIdList.execute().asObservable().firstOrError()
                .map { it.size <= 1 }
        }
    }

    private fun isNotificationForIndividualScope(type: String?): Boolean {
        return when (type) {
            REWARD.value, LOGOUT.value, INDIVIDUAL.value, CONTACT_NETWORK.value -> true
            else -> false
        }
    }

    class Factory @Inject constructor(
        private val api: Lazy<CommunicationRepository>,
        private val communicationRemoteSource: Lazy<CommunicationRemoteSource>,
        private val communicationTracker: Lazy<CommunicationTracker>,
        private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
        private val getBusinessIdList: Lazy<GetBusinessIdList>,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters) =
            CommunicationProcessSyncNotificationWorker(
                context,
                params,
                api.get(),
                communicationRemoteSource.get(),
                communicationTracker.get(),
                getActiveBusinessId.get(),
                getBusinessIdList.get(),
            )
    }
}
