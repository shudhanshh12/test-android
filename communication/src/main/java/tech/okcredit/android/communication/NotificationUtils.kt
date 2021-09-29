package tech.okcredit.android.communication

import `in`.okcredit.merchant.device.marshmallow
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.clevertap.android.sdk.CleverTapAPI
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.Lazy
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.communication.NotificationData.Companion.KEY_NOTIFICATION_INTENT_EXTRA
import tech.okcredit.android.communication.brodcaste_receiver.NotificationActionBroadcastReceiver
import timber.log.Timber
import javax.inject.Inject

class NotificationUtils @Inject constructor(
    private val context: Lazy<Context>
) {

    companion object {

        private const val CLEVER_TAP_CUSTOM_API_KEY = "type"
        private const val CLEVER_TAP_IS_STICKY_KEY = "is_sticky"
        private const val CLEVER_TAP_CUSTOM_API_DAILY_REPORT_VALUE = "daily_report"

        fun getDailyReportNotificationData(context: Context): NotificationData {
            return NotificationData(
                primaryAction = CommunicationConstants.PRIMARY_ACTION_DAILY_REPORT,
                btnPrimaryLabel = context.getString(R.string.dismiss),
                btnSecondaryLabel = context.getString(R.string.mark_as_read),
                btnPrimaryAction = PreDefinedAction.DISMISS.value,
                btnSecondaryAction = PreDefinedAction.MARK_AS_READ.value,
                renderType = "daily_report",
                campaignId = "daily_report"
            )
        }

        fun assignNotificationId(): Int {
            return (System.currentTimeMillis() % 100000).toInt()
        }

        fun RemoteMessage.isCleverTapDailyReportNotification(): Boolean {
            var returnValue = false
            for ((key, value) in data) {
                if (key == CLEVER_TAP_CUSTOM_API_KEY && value == CLEVER_TAP_CUSTOM_API_DAILY_REPORT_VALUE) {
                    returnValue = true
                    break
                }
            }

            return returnValue
        }

        fun RemoteMessage.isCleverTapStickyNotification(): Boolean {
            var returnValue = false
            val extras = getBundledData()
            val info = CleverTapAPI.getNotificationInfo(extras)
            if (info.fromCleverTap) {
                extras[CLEVER_TAP_IS_STICKY_KEY]?.let {
                    returnValue = it.toString().toBoolean()
                }
            }
            return returnValue
        }

        fun RemoteMessage.isCleverTapNotification(): Boolean {
            var returnValue = false
            val extras = getBundledData()
            return CleverTapAPI.getNotificationInfo(extras).fromCleverTap
        }

        fun RemoteMessage.getBundledData(): Bundle {
            val extras = Bundle()
            for ((key, value) in data) {
                extras.putString(key, value)
            }
            return extras
        }

        fun RemoteMessage.isOKCreditSyncNotification(): Boolean {
            return this.data["type"] != null
        }

        fun RemoteMessage.isOKDailyReportNotification(): Boolean {
            return this.data["render_type"] == PreDefinedNotificationRenderStyle.DAILY_REPORT.value &&
                this.data["visible"] == "true"
        }

        fun RemoteMessage.isFromZendesk(): Boolean {
            return this.data["data"]?.let { data ->
                data.contains("zd.chat.msg") or data.contains("zd.chat.end")
            } ?: false
        }

        // Get Arraylist of actions from a notifications
        fun getActionsOfNotifications(
            notificationData: NotificationData,
            context: Context
        ): List<NotificationCompat.Action> {
            val actions = arrayListOf<NotificationCompat.Action>()

            if (notificationData.btnPrimaryLabel.isNullOrEmpty().not()) {
                val intent = Intent(
                    context,
                    NotificationActionBroadcastReceiver::class.java
                ).apply {
                    action = NotificationActionBroadcastReceiver.Companion.ACTIONS.PRIMARY.toString()
                    putExtra(KEY_NOTIFICATION_INTENT_EXTRA, Gson().toJson(notificationData))
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    assignNotificationId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                actions.add(NotificationCompat.Action(0, notificationData.btnPrimaryLabel!!, pendingIntent))
            }

            if (notificationData.btnSecondaryLabel.isNullOrEmpty().not()) {
                val intent = Intent(
                    context,
                    NotificationActionBroadcastReceiver::class.java
                ).apply {
                    action = NotificationActionBroadcastReceiver.Companion.ACTIONS.SECONDARY.toString()
                    putExtra(KEY_NOTIFICATION_INTENT_EXTRA, Gson().toJson(notificationData))
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    assignNotificationId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                actions.add(NotificationCompat.Action(0, notificationData.btnSecondaryLabel!!, pendingIntent))
            }

            return actions
        }

        fun clearAllNotifications(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }
    }

    fun getCurrentNotifications(): MutableList<NotificationDataWrapper> {
        val currentNotificationDatas = mutableListOf<NotificationDataWrapper>()

        val notificationManager = context.get().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var notifications = arrayOfNulls<StatusBarNotification>(0)

        marshmallow {
            try {
                notifications = notificationManager.activeNotifications
            } catch (e: Exception) {
                RecordException.recordException(e)
            }
        }
        Timber.d("${CommunicationRepositoryImpl.TAG} Found ${notifications.size} Existing Notification")

        for (notification in notifications) {
            val dataString = notification?.notification?.extras?.getString(KEY_NOTIFICATION_INTENT_EXTRA)
            if (dataString != null) {
                Timber.d("${CommunicationRepositoryImpl.TAG} Existing Notification:$dataString")
                val data = NotificationData.from(dataString)
                currentNotificationDatas.add(
                    NotificationDataWrapper(
                        notification.id,
                        data
                    )
                )
            }
        }
        // reversing the notifications
//            currentNotificationDatas.reverse()
        return currentNotificationDatas
    }

    fun clearEmptySummeryNotifications() {
        val notificationManager = context.get().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        marshmallow {

            val currentOkCreditNotifications = getCurrentNotifications()
            /**
             *  Summary notification will not automatically get cleared when the last notification of that group is consumed.
             *  Here we are deleting Summery Notifications when there is no notifications Present.
             *  https://blog.danlew.net/2017/02/07/correctly-handling-bundled-android-notifications/
             */
            try {
                if (currentOkCreditNotifications.isEmpty()) {
                    for (notification in notificationManager.activeNotifications) {
                        val isSummeryNotification =
                            notification?.notification?.extras?.getBoolean(NotificationData.KEY_NOTIFICATION_IS_SUMMERY)
                        if (isSummeryNotification == true) {
                            notificationManager.cancel(notification.id)
                        }
                    }
                }
            } catch (e: Exception) {
                RecordException.recordException(e)
            }
        }
    }
}
