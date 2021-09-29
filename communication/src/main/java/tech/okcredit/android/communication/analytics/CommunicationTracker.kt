package tech.okcredit.android.communication.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey.TYPE
import dagger.Lazy
import dagger.Reusable
import javax.inject.Inject

@Reusable
class CommunicationTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    object Key {
        const val FCM_TOKEN = "fcm_token"
        const val ACTION = "Action"
        const val CAMPAIGN_ID = "_campaign_id"
        const val SUB_CAMPAIGN_ID = "_sub_campaign_id"
        const val SEGMENT = "segment"
        const val STEP = "Step"
        const val DATA = "Data"
        const val FLOW_ID = "Flow ID"
        const val APP_IN_FOREGROUND = "app_open"
        const val BUSINESS_ID = "business_id"
        const val TYPE = "type"
        const val IS_SYNC_NOTIFICATION = "is_sync_notification"
        const val NOTIFICATION_ID = "notification_id"
    }

    object Event {
        const val NOTIFICATION_RECEIVED = "NotificationData: Received"
        const val NOTIFICATION_DISPLAYED = "NotificationData: Displayed"
        const val NOTIFICATION_CLICKED = "NotificationData: Clicked"
        const val NOTIFICATION_ACTION_CLICKED = "NotificationData: Action Button Clicked"
        const val NOTIFICATION_TOKEN = "NotificationData: New Token Registered"
        const val NOTIFICATION_ACTION_DEBUG = "NotificationData: Debug"
        const val FCM_NOTIFICATION_ERROR_BUSINESS_ID_MISSING = "fcm_notification_error_business_id_missing"
        const val VISIBLE_FCM_NOTIFICATION_IGNORED = "visible_fcm_notification_ignored"
    }

    fun trackNotificationReceived(
        type: String?,
        campaignId: String?,
        subCampaignId: String?,
        segment: String?,
        notificationData: String?,
        foreground: Boolean,
    ) {
        val properties = mutableMapOf<String, Any>().apply {
            this[TYPE] = type ?: ""
            this[Key.CAMPAIGN_ID] = campaignId ?: ""
            this[Key.SUB_CAMPAIGN_ID] = subCampaignId ?: ""
            this[Key.SEGMENT] = segment ?: ""
            this[Key.DATA] = notificationData ?: ""
            this[Key.APP_IN_FOREGROUND] = foreground
        }
        analyticsProvider.get().trackEvents(Event.NOTIFICATION_RECEIVED, properties)
    }

    fun trackNotificationDisplayed(action: String?, campaignId: String?, subCampaignId: String?, segment: String?) {
        val properties = mutableMapOf<String, Any>().apply {
            this[Key.ACTION] = action ?: ""
            this[Key.CAMPAIGN_ID] = campaignId ?: ""
            this[Key.SUB_CAMPAIGN_ID] = subCampaignId ?: ""
            this[Key.SEGMENT] = segment ?: ""
        }
        analyticsProvider.get().trackEvents(Event.NOTIFICATION_DISPLAYED, properties)
    }

    fun trackNotificationClicked(action: String?, campaignId: String?, subCampaignId: String?, segment: String?) {
        val properties = mutableMapOf<String, Any>().apply {
            this[Key.ACTION] = action ?: ""
            this[Key.CAMPAIGN_ID] = campaignId ?: ""
            this[Key.SUB_CAMPAIGN_ID] = subCampaignId ?: ""
            this[Key.SEGMENT] = segment ?: ""
        }
        analyticsProvider.get().trackEvents(Event.NOTIFICATION_CLICKED, properties)
    }

    fun trackNotificationActionClicked(action: String?, campaignId: String?, subCampaignId: String?, segment: String?) {
        val properties = mutableMapOf<String, Any>().apply {
            this[Key.ACTION] = action ?: ""
            this[Key.CAMPAIGN_ID] = campaignId ?: ""
            this[Key.SUB_CAMPAIGN_ID] = subCampaignId ?: ""
            this[Key.SEGMENT] = segment ?: ""
        }
        analyticsProvider.get().trackEvents(Event.NOTIFICATION_ACTION_CLICKED, properties)
    }

    fun trackNotificationTokenRegistered(fcmToken: String, isFromNewToken: Boolean) {
        val properties = mutableMapOf<String, Any>().apply {
            this[Key.FCM_TOKEN] = fcmToken ?: ""
            this["FromOnNewToken"] = isFromNewToken
        }
        analyticsProvider.get().trackEvents(Event.NOTIFICATION_TOKEN, properties)
    }

    fun debugNotification(
        step: String? = null,
        flowId: String? = null,
        campaignId: String? = null,
        subCampaignId: String? = null,
        data: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>().apply {
            this[Key.STEP] = step ?: ""
            this[Key.FLOW_ID] = flowId ?: ""
            this[Key.DATA] = data ?: ""
            this[Key.CAMPAIGN_ID] = campaignId ?: ""
            this[Key.SUB_CAMPAIGN_ID] = subCampaignId ?: ""
        }
        analyticsProvider.get().trackEvents(Event.NOTIFICATION_ACTION_DEBUG, properties)
    }

    fun trackBusinessIdMissing(
        businessId: String?,
        isSyncNotification: Boolean,
        type: String?,
        campaignId: String?,
        subCampaignId: String?,
        notificationId: String?,
    ) {
        val properties = mapOf(
            Key.BUSINESS_ID to businessId.toString(),
            Key.TYPE to type.toString(),
            Key.IS_SYNC_NOTIFICATION to isSyncNotification.toString(),
            Key.CAMPAIGN_ID to campaignId.toString(),
            Key.SUB_CAMPAIGN_ID to subCampaignId.toString(),
            Key.NOTIFICATION_ID to notificationId.toString(),
        )
        analyticsProvider.get()
            .trackEngineeringMetricEvents(Event.FCM_NOTIFICATION_ERROR_BUSINESS_ID_MISSING, properties)
    }

    fun trackNotificationIgnored(
        businessId: String?,
        campaignId: String?,
        subCampaignId: String?,
        notificationId: String?,
    ) {
        val properties = mapOf(
            Key.BUSINESS_ID to businessId.toString(),
            Key.CAMPAIGN_ID to campaignId.toString(),
            Key.SUB_CAMPAIGN_ID to subCampaignId.toString(),
            Key.NOTIFICATION_ID to notificationId.toString(),
        )
        analyticsProvider.get()
            .trackEngineeringMetricEvents(Event.VISIBLE_FCM_NOTIFICATION_IGNORED, properties)
    }
}
