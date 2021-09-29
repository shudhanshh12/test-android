package tech.okcredit.android.communication.services

import `in`.okcredit.analytics.ANRDebugger
import `in`.okcredit.backend.contract.DeepLinkUrl
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.device.DeviceRepository
import android.content.Context
import com.appsflyer.AppsFlyerLib
import com.clevertap.android.sdk.CleverTapAPI
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.Lazy
import dagger.android.AndroidInjection
import org.joda.time.DateTime
import org.joda.time.Duration
import tech.okcredit.android.base.utils.AppUtils
import tech.okcredit.android.communication.CommunicationConstants
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.CommunicationRepositoryImpl.Companion.ZENDESK_IN_APP_CHAT_CAMPAIGN
import tech.okcredit.android.communication.NotificationUtils.Companion.getBundledData
import tech.okcredit.android.communication.NotificationUtils.Companion.isCleverTapDailyReportNotification
import tech.okcredit.android.communication.NotificationUtils.Companion.isCleverTapNotification
import tech.okcredit.android.communication.NotificationUtils.Companion.isCleverTapStickyNotification
import tech.okcredit.android.communication.NotificationUtils.Companion.isFromZendesk
import tech.okcredit.android.communication.NotificationUtils.Companion.isOKCreditSyncNotification
import tech.okcredit.android.communication.NotificationUtils.Companion.isOKDailyReportNotification
import tech.okcredit.android.communication.R
import tech.okcredit.android.communication.analytics.CommunicationTracker
import tech.okcredit.android.communication.handlers.NotificationHelperStickyNotifications
import tech.okcredit.base.exceptions.ExceptionUtils
import timber.log.Timber
import zendesk.chat.Chat
import zendesk.chat.PushData
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessagingService : FirebaseMessagingService() {

    @Inject
    internal lateinit var deviceRepository: Lazy<DeviceRepository>

    @Inject
    internal lateinit var cleverTapAPI: Lazy<CleverTapAPI>

    @Inject
    internal lateinit var appsFlyerApi: Lazy<AppsFlyerLib>

    @Inject
    internal lateinit var communicationApi: Lazy<CommunicationRepository>

    @Inject
    internal lateinit var communicationTracker: Lazy<CommunicationTracker>

    @Inject
    internal lateinit var context: Lazy<Context>

    @Inject
    lateinit var anrDebugger: Lazy<ANRDebugger>

    @Inject
    lateinit var getActiveBusinessId: Lazy<GetActiveBusinessId>

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        super.onMessageReceived(msg)
        val data = msg.data
        anrDebugger.get().setLastNotificationType(getTypeFromData(msg))
        when {
            data.containsKey(CommunicationConstants.APPS_FLYER_UNINSTALL_NOTIFICATION) -> { //  Android uninstall push notification from AppsFlyer.
                return
            }
            msg.isCleverTapDailyReportNotification() -> { // Deprecated Notifications
                return
            }
            msg.isCleverTapStickyNotification() -> { //  Sticky Notification from CleverTap.
                NotificationHelperStickyNotifications.stickyNotification(applicationContext, msg.getBundledData())
                return
            }
            msg.isFromZendesk() -> {
                scheduleZendeskNotification(data)
            }
            msg.isCleverTapNotification() -> { // CleverTap Notification.
                CleverTapAPI.createNotification(applicationContext, msg.getBundledData())
                try {
                    CleverTapAPI.processPushNotification(applicationContext, msg.getBundledData())
                } catch (e: Exception) {
                    ExceptionUtils.logException(e)
                }
                // ensure duplicate Push notification is not rendered via Push Amplification

                return
            }
            data.isEmpty() -> {
                return
            }
            msg.isOKDailyReportNotification() -> { // OKCredit Silent Notification for Syncing data.
                communicationApi.get().sendDailyReportNotification()

                communicationTracker.get().trackNotificationReceived(
                    type = "Daily Report Notification",
                    campaignId = data["_campaign_id"],
                    subCampaignId = data["_subcampaign_id"],
                    segment = data["segment"],
                    notificationData = msg.data.toString(),
                    foreground = AppUtils.isAppForegrounded(),
                )
            }
            msg.isOKCreditSyncNotification() -> { // OKCredit Silent Notification for Syncing data.
                val type = data["type"]
                // Deprecated Notification types
                when {
                    DEPRICATED_NOTIFICATION_TYPES.contains(type) -> {
                        return
                    }
                    canIgnoreNotification(type!!) -> {
                        return
                    }
                }
                Timber.i("$TAG Syncing Notification For Type: %s", type)

                communicationTracker.get().trackNotificationReceived(
                    type = "Silent Notification $type",
                    campaignId = data["_campaign_id"],
                    subCampaignId = data["_subcampaign_id"],
                    segment = data["segment"],
                    notificationData = msg.data.toString(),
                    foreground = AppUtils.isAppForegrounded(),
                )

                communicationApi.get().scheduleSyncNotification(msg)
            }
            else -> { // OKCredit Notification.
                val campaignId = data["_campaign_id"]
                val subCampaignId = data["_subcampaign_id"]
                val segment = data["segment"]
                msg.data["receive_time"] = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()

                communicationTracker.get().debugNotification(
                    "Step_1_Received",
                    data["notification_id"],
                    campaignId,
                    subCampaignId,
                    msg.data.toString()
                )

                communicationTracker.get().trackNotificationReceived(
                    type = "Server Notification",
                    campaignId = campaignId,
                    subCampaignId = subCampaignId,
                    segment = segment,
                    notificationData = msg.data.toString(),
                    foreground = AppUtils.isAppForegrounded(),
                )

                communicationApi.get().scheduleProcessingOkCreditNotification(Gson().toJson(msg.data))
            }
        }
    }

    private fun getTypeFromData(msg: RemoteMessage): String {
        val data = msg.data
        return when {
            msg.isCleverTapDailyReportNotification() -> { // Deprecated Notifications
                "clevertap_daily_report"
            }
            msg.isCleverTapStickyNotification() -> { //  Sticky Notification from CleverTap.
                "clevertap_sticky"
            }
            msg.isFromZendesk() -> {
                "zendesk"
            }
            msg.isCleverTapNotification() -> { // CleverTap Notification.
                "clevertap"
            }
            data.isEmpty() -> {
                "empty_data"
            }
            msg.isOKDailyReportNotification() -> { // OKCredit Silent Notification for Syncing data.
                "daily_report"
            }
            msg.isOKCreditSyncNotification() -> { // OKCredit Silent Notification for Syncing data.
                val type = data["type"]
                "sync_$type"
            }
            else -> { // OKCredit Notification.
                val campaignId = data["_campaign_id"]
                "display_$campaignId"
            }
        }
    }

    private fun scheduleZendeskNotification(data: Map<String, String>) {
        val pushData: PushData? =
            Chat.INSTANCE.providers()?.pushNotificationsProvider()?.processPushNotification(data)

        if (pushData != null) {
            val map: MutableMap<String, String> = HashMap()

            map["content"] = if (pushData.type == PushData.Type.MESSAGE) "${pushData.message}"
            else context.get().getString(R.string.in_app_help_chat_ended, pushData.author)
            map["primary_action"] = DeepLinkUrl.MANUAL_CHAT
            map["title"] = context.get().getString(R.string.in_app_help_title, pushData.author)
            map["receive_time"] = pushData.timestamp.toString()
            map["visible"] = "true"
            map["image_id"] = (R.drawable.ic_cs_icon).toString()
            map["expire_time"] = "2889326032"
            map["btn_enabled"] = "false"
            map["_campaign_id"] = ZENDESK_IN_APP_CHAT_CAMPAIGN
            map["segment"] = "this_is_segment"
            map["notification_handler_key"] = "zendesk_customer_support"
            map["notification_handler_name"] = context.get().getString(R.string.in_app_help_title, pushData.author)
            val value = Gson().toJson(map)

            communicationApi.get().scheduleProcessingOkCreditNotification(value)
        }
    }

    override fun onNewToken(fcmToken: String) {
        Timber.i("[FCM] got new token (%s)", fcmToken)
        communicationTracker.get().trackNotificationTokenRegistered(fcmToken, true)
        deviceRepository.get().setFcmToken(fcmToken)
        cleverTapAPI.get().pushFcmRegistrationId(fcmToken, true)
        appsFlyerApi.get().updateServerUninstallToken(applicationContext, fcmToken)
    }

    //  Added for fixing frequent notification issue from backend. okcredit won't process more than
// `CUSTOMER_SYNC_NOTIFICATION_LIMIT` Customer & Supplier Notifications in `CUSTOMER_SYNC_LIMIT_DURATION` min.
    private fun canIgnoreNotification(type: String): Boolean {
        var returnValue = false
        if (type == CUSTOMER_SYNC_TYPE || type == SUPPLIER_SYNC_TYPE) {
            val duration =
                Duration(
                    DateTime(lastProcessedCustomerAndSupplierNotificationsTime),
                    DateTime.now()
                ).standardMinutes
            if (totalCustomerAndSupplierSyncNotifications > CUSTOMER_SYNC_NOTIFICATION_LIMIT) {
                if (duration >= CUSTOMER_SYNC_LIMIT_DURATION) {
                    totalCustomerAndSupplierSyncNotifications = 0
                } else {
                    returnValue = true
                }
            }
            totalCustomerAndSupplierSyncNotifications++
            lastProcessedCustomerAndSupplierNotificationsTime = DateTime.now().millis
        } else {
            returnValue = false
        }
        return returnValue
    }

    companion object {
        private const val TAG = "<<<<Notification"

        // TODO: Should refer from SyncTypes on SyncNotificationHandler.
        private const val CUSTOMER_SYNC_TYPE = "customer_v2"
        private const val SUPPLIER_SYNC_TYPE = "supplier_v2"

        private var totalCustomerAndSupplierSyncNotifications = 0
        private var lastProcessedCustomerAndSupplierNotificationsTime = System.currentTimeMillis()
        private const val CUSTOMER_SYNC_NOTIFICATION_LIMIT = 10
        private const val CUSTOMER_SYNC_LIMIT_DURATION = 3
        const val CLEVER_TAP_REGISTRATION_CHANNEL_ID = "CleverTapNotificationChannel"
        const val CLEVER_TAP_REGISTRATION_CHANNEL_NAME = "Miscellaneous"
        const val CLEVER_TAP_REGISTRATION_CHANNEL_DESCRIPTION = "Miscellaneous Notification"
        private val DEPRICATED_NOTIFICATION_TYPES = arrayListOf("customer", "supplier")
    }
}
