package tech.okcredit.android.communication.brodcaste_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.Lazy
import dagger.android.AndroidInjection
import tech.okcredit.android.communication.NotificationUtils
import javax.inject.Inject

class NotificationDeleteReceiver : BroadcastReceiver() {

    @Inject
    internal lateinit var notificationUtils: Lazy<NotificationUtils>

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        notificationUtils.get().clearEmptySummeryNotifications()
    }
}
