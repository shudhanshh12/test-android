package tech.okcredit.android.communication.handlers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import tech.okcredit.android.base.TempCurrencyUtil
import tech.okcredit.android.communication.CommunicationConstants
import tech.okcredit.android.communication.DailyReportResponce
import tech.okcredit.android.communication.NotificationData.Companion.KEY_NOTIFICATION_INTENT_EXTRA
import tech.okcredit.android.communication.NotificationUtils
import tech.okcredit.android.communication.R

object NotificationHelperForDailyReport {

    // Handling Logic of Visibility & Expiry of Notification
    fun renderNotification(
        context: Context,
        dailyReportResponce: DailyReportResponce,
        channelId: String,
        pendingIntent: PendingIntent
    ) {

        val expandView = RemoteViews(context.packageName, R.layout.remote_view_expand_notification)
        val notificationTitle = context.getString(
            R.string.weekly_report_notification_content,
            if (dailyReportResponce.netBalance < 0) context.getString(R.string.due) else context.getString(R.string.advance),
            (String.format("₹%s", TempCurrencyUtil.formatV2(dailyReportResponce.netBalance)))
        )
        expandView.setTextViewText(
            R.id.content_text, notificationTitle
        )
        expandView.setTextViewText(
            R.id.amount, (String.format("₹%s", TempCurrencyUtil.formatV2(dailyReportResponce.netBalance)))
        )

        if (dailyReportResponce.netBalance < 0) {
            expandView.setTextColor(R.id.amount, ContextCompat.getColor(context, R.color.tx_credit))
        } else {
            expandView.setTextColor(R.id.amount, ContextCompat.getColor(context, R.color.tx_payment))
        }

        expandView.setTextViewText(
            R.id.tv_payment_amount, (String.format("₹%s", TempCurrencyUtil.formatV2(dailyReportResponce.netPayment)))
        )
        expandView.setTextViewText(
            R.id.tv_credit_amount, (String.format("₹%s", TempCurrencyUtil.formatV2(-1 * dailyReportResponce.netCredit)))
        )

        val collapseView = RemoteViews(context.packageName, R.layout.remote_view_collapsed_notification)
        collapseView.setTextViewText(
            R.id.timestamp,
            DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME)
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        lateinit var messageNotificationBuilder: NotificationCompat.Builder

        val bundle = Bundle()
        bundle.putString(
            KEY_NOTIFICATION_INTENT_EXTRA,
            Gson().toJson(NotificationUtils.getDailyReportNotificationData(context))
        )

        messageNotificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.noti_small_icon)
            .setColor(ContextCompat.getColor(context, R.color.green_primary))
            .setContentTitle(context.getString(R.string.daily_report))
            .setContentText(notificationTitle)
            .setContentIntent(pendingIntent)
            .setCustomContentView(collapseView)
            .setCustomBigContentView(expandView)
            .setExtras(bundle)
            .setAutoCancel(true)
            .setGroup(CommunicationConstants.GROUP_SUMMREY_KEY)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        val actions =
            NotificationUtils.getActionsOfNotifications(
                NotificationUtils.getDailyReportNotificationData(context),
                context
            )
        for (action in actions) {
            messageNotificationBuilder.addAction(action)
        }

        val notificationId = NotificationUtils.assignNotificationId()
        notificationManager.notify(notificationId, messageNotificationBuilder.build())
    }
}
