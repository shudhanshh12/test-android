package tech.okcredit.android.communication.handlers

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.R
import tech.okcredit.android.communication.SYNC_PROGRESS_NOTIFICATION_CHANNEL
import javax.inject.Inject

class NotificationHelperSyncProgressNotification @Inject constructor(
    private val context: Context,
    private val communicationApi: CommunicationRepository
) {
    fun showNotification() {
        createNotificationChannel()
        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.noti_small_icon)
                .setContentTitle(context.getString(R.string.syncing_your_account))
                .setOngoing(true)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setProgress(100, 100, true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTimeoutAfter(300000L) // Don't show for more than 5 mins.
                .setAutoCancel(false)
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID, builder.build())
    }

    fun hideNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            communicationApi.createNotificationChannel(SYNC_PROGRESS_NOTIFICATION_CHANNEL)
        }
    }

    companion object {
        const val CHANNEL_ID = "sync"
        private const val NOTIFICATION_ID = 7
    }
}
