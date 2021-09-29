package tech.okcredit.android.communication.workers

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.GetBusinessIdList
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
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
import tech.okcredit.android.communication.CommunicationRepositoryImpl.Companion.ZENDESK_IN_APP_CHAT_CAMPAIGN
import tech.okcredit.android.communication.NotificationData
import tech.okcredit.android.communication.analytics.CommunicationTracker
import tech.okcredit.base.exceptions.ExceptionUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CommunicationProcessNotificationWorker constructor(
    context: Context,
    params: WorkerParameters,
    private val communicationApi: CommunicationRepository,
    private val communicationRemoteSource: CommunicationRemoteSource,
    private val communicationTracker: CommunicationTracker,
    private val getBusinessIdList: GetBusinessIdList,
    private val getActiveBusinessId: GetActiveBusinessId,
) : BaseRxWorker(context, params) {

    override fun doRxWork(): Completable {
        val notificationData: NotificationData?
        val messageData = inputData.getString(CommunicationRepositoryImpl.MESSAGE_DATA)
        try {
            notificationData = Gson().fromJson(messageData, NotificationData::class.java)
        } catch (e: Exception) {
            ExceptionUtils.logException("Error: Visible NotificationData JSON Parsing", e)
            return Completable.complete()
        }

        communicationTracker.debugNotification(
            "Step_3_Executing",
            notificationData?.notificationId,
            notificationData?.campaignId,
            notificationData?.subCampaignId,
            messageData
        )

        return if (notificationData != null) {
            canShowNotification(notificationData.businessId)
                .flatMapCompletable { canShowNotification ->
                    if (canShowNotification || isFromZendesk(notificationData.campaignId)) {
                        getActiveBusinessId.thisOrActiveBusinessId(notificationData.businessId)
                            .flatMapCompletable { businessId ->
                                showNotification(notificationData, messageData, businessId)
                            }
                    } else {
                        communicationTracker.trackNotificationIgnored(
                            businessId = notificationData.businessId,
                            campaignId = notificationData.campaignId,
                            subCampaignId = notificationData.subCampaignId,
                            notificationId = notificationData.notificationId,
                        )
                        Completable.complete() // Ignore notification
                    }
                }
        } else {
            Completable.complete()
        }
    }

    private fun isFromZendesk(campaignId: String?): Boolean {
        return campaignId == ZENDESK_IN_APP_CHAT_CAMPAIGN
    }

    // Ignore notification if multiple accounts are added and businessId is missing in FCM
    private fun canShowNotification(businessId: String?): Single<Boolean> {
        return if (businessId.isNotNullOrBlank()) {
            Single.just(true)
        } else {
            getBusinessIdList.execute().asObservable().firstOrError()
                .map { it.size <= 1 }
        }
    }

    private fun showNotification(
        notificationData: NotificationData,
        messageData: String?,
        businessId: String,
    ): Completable {
        return communicationApi.executeOkCreditNotification(notificationData)
            .timeout(20, TimeUnit.SECONDS).onErrorComplete()
            .andThen(
                if (notificationData.notificationId.isNullOrEmpty().not()) {
                    communicationTracker.debugNotification(
                        "Step_7_Ack",
                        notificationData.notificationId,
                        notificationData.campaignId,
                        notificationData.subCampaignId,
                        messageData
                    )
                    communicationRemoteSource.acknowledge(notificationData.notificationId!!, businessId)
                        .doOnError { RecordException.recordException(it) }
                        .onErrorComplete()
                } else {
                    Completable.complete()
                }
            )
    }

    class Factory @Inject constructor(
        private val api: Lazy<CommunicationRepository>,
        private val communicationRemoteSource: Lazy<CommunicationRemoteSource>,
        private val communicationTracker: Lazy<CommunicationTracker>,
        private val getBusinessIdList: Lazy<GetBusinessIdList>,
        private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return CommunicationProcessNotificationWorker(
                context,
                params,
                api.get(),
                communicationRemoteSource.get(),
                communicationTracker.get(),
                getBusinessIdList.get(),
                getActiveBusinessId.get(),
            )
        }
    }
}
