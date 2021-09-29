package `in`.okcredit.di.binding.communications

import `in`.okcredit.backend.contract.Constants
import `in`.okcredit.notification.ResolveIntentsAndExtrasFromDeeplinkImpl
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import tech.okcredit.android.communication.GetNotificationIntentBinding
import tech.okcredit.android.communication.NotificationData
import javax.inject.Inject

class GetNotificationIntentBindingImpl @Inject constructor(private val context: Context) :
    GetNotificationIntentBinding {
    override fun getNotificationIntentBinding(
        action: String?,
        notificationData: NotificationData?
    ): PendingIntent {
        return if (
            action != null &&
            (action.contains(Constants.DEEPLINK_BASE_URL) || action.contains(Constants.DEEPLINK_V2_BASE_URL))
        ) {
            ResolveIntentsAndExtrasFromDeeplinkImpl.deepLinkScreen(context, action, notificationData)
        } else if (action != null) {
            val notificationIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(action))
            PendingIntent.getActivity(context, 0, notificationIntent, 0)
        } else {
            PendingIntent.getActivity(context, 0, null, 0)
        }
    }
}
