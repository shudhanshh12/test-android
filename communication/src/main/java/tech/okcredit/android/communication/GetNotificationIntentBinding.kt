package tech.okcredit.android.communication

import android.app.PendingIntent

interface GetNotificationIntentBinding {
    fun getNotificationIntentBinding(action: String?, notificationData: NotificationData?): PendingIntent
}
