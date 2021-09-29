package `in`.okcredit.analytics.helpers

import `in`.okcredit.analytics.BuildConfig
import `in`.okcredit.analytics.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.text.Html
import android.text.SpannableString
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.Lazy
import dagger.Reusable
import tech.okcredit.android.base.CircularQueue
import timber.log.Timber
import javax.inject.Inject

@Reusable
class AnalyticsNotificationHelper @Inject constructor(val context: Lazy<Context>) {

    companion object {
        const val CHANNEL_ID = "analytics"
        const val NOTIFICATION_ID = 123123
        const val NO_OF_ITEMS = 10
    }

    var analyticsEventQueue = CircularQueue<String>(NO_OF_ITEMS)

    fun addInNotification(eventName: String, propString: String) {

        // Uncomment this for analytics notification locally
        if (BuildConfig.BUILD_TYPE != "qa") {
            return
        }

        Timber.d("<<<<analyticsEventQueue size=${analyticsEventQueue.size}")

        createNotificationChannel()
        analyticsEventQueue
            .add("<b>$eventName:</b>$propString<br>")

        var body = ""
        for (item in analyticsEventQueue.reversed()) {
            body += item
        }

        Timber.d("<<<<analyticsEventQueue body=$body")

        val formattedBody = SpannableString(Html.fromHtml(body))

        val builder = NotificationCompat.Builder(context.get(), CHANNEL_ID)
            .setSmallIcon(R.drawable.noti_small_icon)
            .setStyle(NotificationCompat.BigTextStyle().bigText(formattedBody))
            .setContentText(formattedBody)
            .setOngoing(true)
            .setColor(ContextCompat.getColor(context.get(), R.color.primary))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setTimeoutAfter(300000L) // don't show for more than 5 mins
            .setAutoCancel(false)

        NotificationManagerCompat.from(context.get()).notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Analytics"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.get().getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
