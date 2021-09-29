package tech.okcredit.android.communication

import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.device.oreo
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import androidx.work.*
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.asObservable
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.extensions.itOrBlank
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.communication.analytics.CommunicationTracker
import tech.okcredit.android.communication.brodcaste_receiver.ApplicationShareReceiver
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.android.communication.handlers.NotificationHelperForCommonNotifications
import tech.okcredit.android.communication.handlers.NotificationHelperForDailyReport
import tech.okcredit.android.communication.workers.CommunicationProcessNotificationWorker
import tech.okcredit.android.communication.workers.CommunicationProcessSyncNotificationWorker
import tech.okcredit.android.communication.workers.CommunicationProcessSyncNotificationWorker.Companion.BUSINESS_ID
import tech.okcredit.base.exceptions.ExceptionUtils
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CommunicationRepositoryImpl @Inject constructor(
    private val authService: Lazy<AuthService>,
    private val context: Context,
    private val workManager: OkcWorkManager,
    private val communicationTracker: Lazy<CommunicationTracker>,
    private val customerBinding: Lazy<GetCustomerBinding>,
    private val supplierBinding: Lazy<GetSupplierBinding>,
    private val getSyncNotificationJobBinding: Lazy<GetSyncNotificationJobBinding>,
    private val getNotificationIntentBinding: Lazy<GetNotificationIntentBinding>,
    private val getDailyReportDetailsBinding: Lazy<GetDailyReportDetailsBinding>,
    private val notificationHelperForCommonNotifications: Lazy<NotificationHelperForCommonNotifications>,
    private val intentHelper: Lazy<IntentHelper>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
) : CommunicationRepository {

    companion object {
        @NonNls
        const val WORKER_PROCESS_VISIBLE_NOTIFICATION = "communication/processVisibleNotification"

        @NonNls
        const val WORKER_PROCESS_SYNC_NOTIFICATION = "communication/processSyncNotification"

        @NonNls
        const val MESSAGE_DATA = "message_data"

        @NonNls
        const val TAG = "<<<<Notification"

        @NonNls
        const val ZENDESK_IN_APP_CHAT_CAMPAIGN = "zendesk_in_app_chat"
    }

    override fun createNotificationChannel(channelOkC: OkCNotificationChannel): OkCNotificationChannel {
        // NotificationChannels are required for Notifications on O (API 26) and above.
        oreo {
            val notificationChannel =
                android.app.NotificationChannel(channelOkC.channelId, channelOkC.name, channelOkC.importance)

            notificationChannel.apply {
                this.description = channelOkC.descriptionText
                this.enableVibration(channelOkC.enableVibrate)
                this.lockscreenVisibility = channelOkC.channelLockScreenVisibility
            }

            // Adds NotificationChannel to system. Attempting to create an existing notification
            // channel with its original values performs no operation, so it's safe to perform the below sequence.
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        return channelOkC
    }

    override fun executeOkCreditNotification(notificationData: NotificationData): Completable {
        return if (authService.get().isAuthenticated()) {
            val notificationIntent =
                getNotificationIntentBinding.get()
                    .getNotificationIntentBinding(notificationData.primaryAction ?: "", notificationData)
            val notificationChannel = createNotificationChannel(TRANSACTIONS_NOTIFICATION_CHANNEL)

            communicationTracker.get().debugNotification(
                "Step_4_Auth_Pass",
                notificationData.notificationId,
                notificationData.campaignId,
                notificationData.subCampaignId,
                notificationData.toString()
            )

            when {
                notificationData.customerId.isNullOrEmpty().not() -> {
                    communicationTracker.get().debugNotification(
                        "Step_4_1_Get_Relation",
                        notificationData.notificationId,
                        notificationData.campaignId,
                        notificationData.subCampaignId,
                        notificationData.toString()
                    )

                    Timber.d("$TAG Getting Notification Handle Details")
                    customerBinding.get().getCustomerNameAndImage(notificationData.customerId!!)
                        .flatMapCompletable {
                            val updatedNotificationData = notificationData.copy(
                                notificationHandlerKey = it.id,
                                notificationHandlerName = it.name,
                                notificationHandlerUrl = it.profile_url
                            )

                            notificationHelperForCommonNotifications.get().handleNotification(
                                updatedNotificationData,
                                context,
                                notificationIntent,
                                communicationTracker.get(),
                                notificationChannel.channelId
                            )
                        }
                }
                notificationData.supplierId.isNullOrEmpty().not() -> {
                    communicationTracker.get().debugNotification(
                        "Step_4_1_Get_Relation",
                        notificationData.notificationId,
                        notificationData.campaignId,
                        notificationData.subCampaignId,
                        notificationData.toString()
                    )

                    Timber.d("$TAG Getting Notification Handle Details")
                    supplierBinding.get().getSupplierNameAndImage(notificationData.supplierId!!)
                        .flatMapCompletable {
                            Timber.d("Supplier Image and Name" + it.name)
                            val updatedNotificationData = notificationData.copy(
                                notificationHandlerKey = it.id,
                                notificationHandlerName = it.name,
                                notificationHandlerUrl = it.profile_url
                            )

                            notificationHelperForCommonNotifications.get().handleNotification(
                                updatedNotificationData,
                                context,
                                notificationIntent,
                                communicationTracker.get(),
                                notificationChannel.channelId
                            )
                        }
                }
                else -> {
                    notificationHelperForCommonNotifications.get().handleNotification(
                        notificationData,
                        context,
                        notificationIntent,
                        communicationTracker.get(),
                        notificationChannel.channelId
                    )
                }
            }
        } else {
            Completable.complete()
        }
    }

    override fun executeSyncNotification(notificationData: NotificationData, businessId: String): Completable {
        return if (authService.get().isAuthenticated()) {
            getSyncNotificationJobBinding.get().getSyncNotificationJobBinding(notificationData, businessId)
        } else {
            Completable.complete()
        }
    }

    override fun sendDailyReportNotification() {
        val notificationData = NotificationUtils.getDailyReportNotificationData(context)
        val pendingIntent = getNotificationIntentBinding.get().getNotificationIntentBinding(
            action = notificationData.primaryAction ?: "",
            notificationData = notificationData
        )
        getBusinessIdList.get().execute().asObservable().firstOrError().map { it.size }
            .flatMapCompletable { businessCount ->
                if (businessCount <= 1) {
                    getDailyReportDetailsBinding.get().getSendReminderBinding().flatMapCompletable {
                        val notificationChannel = createNotificationChannel(TRANSACTIONS_NOTIFICATION_CHANNEL)
                        NotificationHelperForDailyReport.renderNotification(
                            context = context,
                            dailyReportResponce = it,
                            channelId = notificationChannel.channelId,
                            pendingIntent = pendingIntent
                        )
                        trackNotificationDisplayed(notificationData)
                        return@flatMapCompletable Completable.complete()
                    }
                } else {
                    ignoreDailyReportNotification(notificationData)
                }
            }.subscribe()
    }

    private fun ignoreDailyReportNotification(notificationData: NotificationData): Completable {
        communicationTracker.get().trackNotificationIgnored(
            businessId = notificationData.businessId,
            campaignId = notificationData.campaignId,
            subCampaignId = notificationData.subCampaignId,
            notificationId = notificationData.notificationId,
        )
        return Completable.complete()
    }

    private fun trackNotificationDisplayed(
        notificationData: NotificationData,
    ) {
        val primaryAction = notificationData.primaryAction ?: ""
        val campaignId = notificationData.campaignId ?: ""
        val subCampaignId = notificationData.subCampaignId ?: ""
        val segment = notificationData.segment ?: ""
        communicationTracker.get().trackNotificationDisplayed(primaryAction, campaignId, subCampaignId, segment)
        communicationTracker.get().debugNotification(
            "Step_6_Render",
            notificationData.notificationId,
            notificationData.campaignId,
            notificationData.subCampaignId,
            notificationData.toString()
        )
    }

    override fun getIntentFromPrimaryAction(action: String): PendingIntent {
        return getNotificationIntentBinding.get().getNotificationIntentBinding(action, null)
    }

    override fun getApplicationShareReceiverIntent(
        sendIntent: Intent,
        shareType: String,
        contentType: String?,
    ): PendingIntent {
        val receiver = Intent(context, ApplicationShareReceiver::class.java)

        receiver.apply {
            sendIntent.getPackage()?.let {
                this.putExtra(ApplicationShareReceiver.EXTRA_PACKAGE, it)
            }

            contentType?.let {
                this.putExtra(ApplicationShareReceiver.EXTRA_CONTENT_TYPE, it)
            }

            this.putExtra(ApplicationShareReceiver.SHARE_TYPE, shareType)
        }

        return PendingIntent.getBroadcast(context, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun goToWhatsApp(shareIntentBuilder: ShareIntentBuilder): Single<Intent> {

        if (shareIntentBuilder.imageFrom != null) {
            return intentHelper.get().checkUri(shareIntentBuilder.imageFrom).flatMap {
                shareIntentBuilder.uri = it
                return@flatMap intentHelper.get().getWhatsAppIntent(shareIntentBuilder)
            }
        } else {
            return intentHelper.get().getWhatsAppIntent(shareIntentBuilder)
        }
    }

    override fun goToWhatsAppWithTextOnlyExtendedBahaviuor(shareIntentBuilder: ShareIntentBuilder): Single<Intent> {

        return intentHelper.get().getWhatsAppIntentForTextOnlyWithExtendedBehaviour(shareIntentBuilder)
    }

    override fun goToSharableApp(shareIntentBuilder: ShareIntentBuilder): Single<Intent> {
        if (shareIntentBuilder.imageFrom != null) {
            return intentHelper.get().checkUri(shareIntentBuilder.imageFrom).flatMap {
                shareIntentBuilder.uri = it
                return@flatMap intentHelper.get().getAllIntent(shareIntentBuilder)
            }
        } else {
            return intentHelper.get().getAllIntent(shareIntentBuilder)
        }
    }

    override fun goToSms(shareIntentBuilder: ShareIntentBuilder): Single<Intent> {
        val defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context)

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("smsto:" + (shareIntentBuilder.phoneNumber ?: ""))
        intent.putExtra("sms_body", shareIntentBuilder.shareText)

        if (defaultSmsPackageName != null) {
            intent.setPackage(defaultSmsPackageName)
        }
        return Single.just(intent)
    }

    override fun scheduleProcessingOkCreditNotification(dataString: String) {
        var notificationData: NotificationData? = null
        try {
            notificationData = Gson().fromJson(dataString, NotificationData::class.java)
        } catch (e: Exception) {
            ExceptionUtils.logException("Error: Visible NotificationData JSON Parsing", e)
        }

        communicationTracker.get().debugNotification(
            "Step_2_Scheduling",
            notificationData?.notificationId,
            notificationData?.campaignId,
            notificationData?.subCampaignId,
            dataString
        )

        val tag = WORKER_PROCESS_VISIBLE_NOTIFICATION
        val businessId = notificationData?.businessId

        if (businessId.isNullOrBlank() && notificationData?.campaignId != ZENDESK_IN_APP_CHAT_CAMPAIGN) {
            communicationTracker.get().trackBusinessIdMissing(
                businessId = notificationData?.businessId,
                isSyncNotification = true,
                type = notificationData?.type,
                campaignId = notificationData?.campaignId,
                subCampaignId = notificationData?.subCampaignId,
                notificationId = notificationData?.notificationId,
            )
        }

        val workRequest = OneTimeWorkRequestBuilder<CommunicationProcessNotificationWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(tag)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .setInputData(workDataOf(MESSAGE_DATA to dataString))
            .build()
            .enableWorkerLogging()

        workManager
            .schedule(
                WORKER_PROCESS_VISIBLE_NOTIFICATION,
                Scope.Business(businessId.itOrBlank()),
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    override fun scheduleSyncNotification(msg: RemoteMessage) {
        var type: String = WORKER_PROCESS_SYNC_NOTIFICATION
        var notificationData: NotificationData? = null
        val dataString = Gson().toJson(msg.data)
        try {
            notificationData = NotificationData.from(dataString)
            notificationData.type?.let {
                type = "$WORKER_PROCESS_SYNC_NOTIFICATION/$it"
            }
        } catch (e: Exception) {
            ExceptionUtils.logException("Error: NotificationData JSON Parsing", e)
        }
        val workRequest = OneTimeWorkRequestBuilder<CommunicationProcessSyncNotificationWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                workDataOf(
                    BUSINESS_ID to notificationData?.businessId
                )
            )
            .addTag(type)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .setInputData(
                Data.Builder()
                    .putString(MESSAGE_DATA, dataString)
                    .build()
            )
            .build()
            .enableWorkerLogging()

        val scope = notificationData?.businessId?.let { Scope.Business(it) } ?: Scope.Individual

        workManager
            .schedule(type, scope, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest)
    }

    override fun clearAllNotifications(): Completable {
        return Completable.fromAction {
            NotificationUtils.clearAllNotifications(context)
        }
    }
}
