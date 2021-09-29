package tech.okcredit.android.communication.handlers

import `in`.okcredit.merchant.device.nougat
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.CTPushNotificationReceiver
import tech.okcredit.android.base.utils.BitmapUtils
import tech.okcredit.android.communication.NotificationUtils
import tech.okcredit.android.communication.R
import tech.okcredit.android.communication.services.MessagingService.Companion.CLEVER_TAP_REGISTRATION_CHANNEL_DESCRIPTION
import tech.okcredit.android.communication.services.MessagingService.Companion.CLEVER_TAP_REGISTRATION_CHANNEL_ID
import tech.okcredit.android.communication.services.MessagingService.Companion.CLEVER_TAP_REGISTRATION_CHANNEL_NAME

object NotificationHelperStickyNotifications {

    fun stickyNotification(context: Context, extras: Bundle) {
        var notificationPriority = NotificationManager.IMPORTANCE_HIGH
        val extraPriority = extras["pr"].toString()
        nougat {
            notificationPriority = when (extraPriority) {
                "max" -> {
                    NotificationManager.IMPORTANCE_MAX
                }
                "high" -> {
                    NotificationManager.IMPORTANCE_HIGH
                }
                else -> {
                    NotificationManager.IMPORTANCE_DEFAULT
                }
            }
        }
        val appLogo = BitmapUtils.drawableToBitmap(
            ContextCompat.getDrawable(
                context,
                R.mipmap.ic_launcher
            )
        )
        val launchIntent = Intent(context, CTPushNotificationReceiver::class.java).apply {
            putExtras(extras)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pIntent = PendingIntent.getBroadcast(
            context, System.currentTimeMillis().toInt(),
            launchIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        CleverTapAPI.createNotificationChannel(
            context,
            CLEVER_TAP_REGISTRATION_CHANNEL_ID,
            CLEVER_TAP_REGISTRATION_CHANNEL_NAME,
            CLEVER_TAP_REGISTRATION_CHANNEL_DESCRIPTION, NotificationManager.IMPORTANCE_MAX, true
        )
        val notificationBuilder = NotificationCompat.Builder(
            context,
            CLEVER_TAP_REGISTRATION_CHANNEL_ID
        )
        notificationBuilder.setAutoCancel(true)
            .setOngoing(true)
            .setPriority(notificationPriority)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.noti_small_icon)
            .setContentTitle(extras["nt"]?.toString())
            .setContentText(extras["nm"]?.toString())
            .setContentIntent(pIntent)
        if (appLogo != null) {
            notificationBuilder.setLargeIcon(appLogo)
        }
        notificationManager.notify(NotificationUtils.assignNotificationId(), notificationBuilder.build())
    }
}
