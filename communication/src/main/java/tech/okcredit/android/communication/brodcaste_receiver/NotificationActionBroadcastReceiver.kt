package tech.okcredit.android.communication.brodcaste_receiver

import `in`.okcredit.merchant.device.marshmallow
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.service.notification.StatusBarNotification
import dagger.Lazy
import dagger.android.AndroidInjection
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.CommunicationRepositoryImpl
import tech.okcredit.android.communication.NotificationData
import tech.okcredit.android.communication.NotificationData.Companion.KEY_NOTIFICATION_INTENT_EXTRA
import tech.okcredit.android.communication.NotificationUtils
import tech.okcredit.android.communication.PreDefinedAction
import tech.okcredit.android.communication.analytics.CommunicationTracker
import timber.log.Timber
import javax.inject.Inject

class NotificationActionBroadcastReceiver : BroadcastReceiver() {

    @Inject
    internal lateinit var communicationTracker: CommunicationTracker

    @Inject
    internal lateinit var communicationApi: CommunicationRepository

    @Inject
    internal lateinit var notificationUtils: Lazy<NotificationUtils>

    override fun onReceive(context: Context, intent: Intent) {
        context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        AndroidInjection.inject(this, context)

        val dataString = intent.extras?.getString(KEY_NOTIFICATION_INTENT_EXTRA, null) ?: return
        val data = NotificationData.from(dataString)

        var notifications = arrayOfNulls<StatusBarNotification>(0)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        marshmallow {
            notifications = notificationManager.activeNotifications
        }

        for (notification in notifications) {
            val itemDataString = notification?.notification?.extras?.getString(KEY_NOTIFICATION_INTENT_EXTRA)
            if (itemDataString != null) {
                Timber.d("${CommunicationRepositoryImpl.TAG} Existing Notification:$itemDataString")
                val notificationData = NotificationData.from(itemDataString)
                if (data.notificationId == notificationData.notificationId) {
                    notificationManager.cancel(notification.id)
                }
            }
        }

        notificationUtils.get().clearEmptySummeryNotifications()

        val primaryAction = if (intent.action == ACTIONS.PRIMARY.toString()) {
            data.btnPrimaryAction
        } else {
            data.btnSecondaryAction
        }

        communicationTracker.trackNotificationActionClicked(
            primaryAction,
            data.campaignId,
            data.supplierId,
            data.segment
        )

        if (primaryAction == PreDefinedAction.DISMISS.value || primaryAction == PreDefinedAction.MARK_AS_READ.value) {
            return
        } else if (primaryAction != null) {
            val pendingIntent = communicationApi.getIntentFromPrimaryAction(primaryAction)
            pendingIntent.intentSender.sendIntent(context, 0, null, null, null)
        }
    }

    companion object {
        enum class ACTIONS {
            PRIMARY,
            SECONDARY
        }
    }
}
