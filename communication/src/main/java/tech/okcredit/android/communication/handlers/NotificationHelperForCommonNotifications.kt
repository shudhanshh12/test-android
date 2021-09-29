package tech.okcredit.android.communication.handlers

import `in`.okcredit.merchant.device.belowNouget
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.google.gson.Gson
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.utils.BitmapUtils
import tech.okcredit.android.base.utils.BitmapUtils.convertToCircle
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.communication.CommunicationConstants
import tech.okcredit.android.communication.GetNotificationIntentBinding
import tech.okcredit.android.communication.NotificationData
import tech.okcredit.android.communication.NotificationData.Companion.KEY_NOTIFICATION_INTENT_EXTRA
import tech.okcredit.android.communication.NotificationData.Companion.KEY_NOTIFICATION_IS_SUMMERY
import tech.okcredit.android.communication.NotificationDataWrapper
import tech.okcredit.android.communication.NotificationUtils
import tech.okcredit.android.communication.R
import tech.okcredit.android.communication.analytics.CommunicationTracker
import tech.okcredit.android.communication.brodcaste_receiver.NotificationDeleteReceiver
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationHelperForCommonNotifications @Inject constructor(
    private val communicationTracker: Lazy<CommunicationTracker>,
    private val getNotificationIntentBinding: Lazy<GetNotificationIntentBinding>,
    private val notificationUtils: Lazy<NotificationUtils>,
) {

    // Handling Logic of Visibility & Expiry of Notification
    fun handleNotification(
        notificationData: NotificationData,
        context: Context,
        pendingIntent: PendingIntent,
        tracker: CommunicationTracker,
        channelId: String,
    ): Completable {
        return Completable.fromCallable {

            communicationTracker.get().debugNotification(
                "Step_5_Before_Render",
                notificationData.notificationId,
                notificationData.campaignId,
                notificationData.subCampaignId,
                notificationData.toString()
            )

            try {
                val visibility = notificationData.getBooleanVisible()
                val isExpired =
                    TimeUnit.MILLISECONDS.toSeconds(DateTimeUtils.currentDateTime().millis) > notificationData.getExpiryTime()

                if (visibility && isExpired.not()) {
                    // Displaying Notification
                    val currentVisibleNotificationsDatas =
                        notificationUtils.get().getCurrentNotifications()

                    displayNotifications(
                        context,
                        notificationData,
                        currentVisibleNotificationsDatas,
                        pendingIntent,
                        tracker,
                        channelId
                    )
                    Completable.complete()
                } else {
                    Completable.complete()
                }
                // Notification Intent
            } catch (e: Exception) {
                Completable.complete()
            }
        }
    }

    // Finding existing notifications and summery of notification.
    private fun displayNotifications(
        context: Context,
        notificationData: NotificationData,
        currentShowingNotifications: MutableList<NotificationDataWrapper>,
        summeryPendingIntent: PendingIntent,
        tracker: CommunicationTracker,
        channelId: String,
    ) {
        reGenerateNewNotificationDatasFromExistingNotifications(notificationData, currentShowingNotifications)

        val summeryNotificationTitle =
            if (notificationData.notificationHandlerKey == null) notificationData.title else notificationData.notificationHandlerName
        val summeryNotificationContent = notificationData.content

        renderNotifications(
            context,
            currentShowingNotifications,
            summeryNotificationTitle,
            summeryNotificationContent,
            channelId,
            summeryPendingIntent
        )
        trackNotificationDisplayed(tracker, notificationData)
    }

    // Render Group Summery Notifications
    private fun renderNotifications(
        context: Context,
        notifications: MutableList<NotificationDataWrapper>,
        summeryNotificationTitle: String?,
        summeryNotificationContent: String?,
        channelId: String,
        pendingIntent: PendingIntent?,
    ) {
        val notificationManager = NotificationManagerCompat.from(context)

        for (notification in notifications) {
            renderNotification(
                context,
                notificationManager,
                notification,
                channelId
            )
        }

//      On Android 7.0 (API level 24) and higher, the system automatically builds a summary
//      for your group using snippets of text from each notification
        belowNouget {
            if (notifications.isNotEmpty()) {
                val bundle = Bundle()
                bundle.putBoolean(KEY_NOTIFICATION_IS_SUMMERY, true)

                val summaryNotificationBuilder =
                    NotificationCompat.Builder(context, channelId)
                        .setContentTitle(summeryNotificationTitle) // set content text to support devices running API level < 24
                        .setContentText(summeryNotificationContent) // set style to support devices running API level < 24
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.noti_small_icon)
                        .setColor(ContextCompat.getColor(context, R.color.green_primary))
                        .setExtras(bundle)
                        .setGroup(CommunicationConstants.GROUP_SUMMREY_KEY)
                        .setGroupSummary(true) // set this notification as the summary for the group

                summaryNotificationBuilder.setStyle(getStyleOfNotification(notifications[0].notificationData, context))
                summaryNotificationBuilder.setContentIntent(pendingIntent)
                notificationManager.notify(NotificationUtils.assignNotificationId(), summaryNotificationBuilder.build())
            }
        }
    }

    // Responsible for a single notification.
    private fun renderNotification(
        context: Context,
        notificationManager: NotificationManagerCompat,
        notification: NotificationDataWrapper,
        channelId: String,
    ) {

        val pendingIntent =
            getNotificationIntentBinding.get().getNotificationIntentBinding(
                notification.notificationData.primaryAction ?: "",
                notification.notificationData
            )

        val notificationData: NotificationData = notification.notificationData

        val bundle = Bundle()
        bundle.putString(KEY_NOTIFICATION_INTENT_EXTRA, Gson().toJson(notification.notificationData))
        val notificationStyle = getStyleOfNotification(notificationData, context)

        val intent = Intent(context, NotificationDeleteReceiver::class.java)
        val deleteReceiver = PendingIntent.getBroadcast(context, 0, intent, 0)

        val messageNotificationBuilder =
            NotificationCompat.Builder(context, channelId)
                .setDeleteIntent(deleteReceiver)
                .setSmallIcon(R.drawable.noti_small_icon)
                .setColor(ContextCompat.getColor(context, R.color.green_primary))
                .setStyle(notificationStyle)
                .setExtras(bundle)
                .setAutoCancel(true)
                .setWhen(notificationData.getReceivedTime() * 1000)
                .setContentTitle(notificationData.getContentTitle())
                .setContentText(notificationData.content)
                .setGroup(CommunicationConstants.GROUP_SUMMREY_KEY)
                .setContentIntent(pendingIntent)

        val actions = NotificationUtils.getActionsOfNotifications(notificationData, context)
        for (action in actions) {
            messageNotificationBuilder.addAction(action)
        }
        notificationManager.notify(notification.id, messageNotificationBuilder.build())
    }

    /*** Helpers ***/
    private fun getStyleOfNotification(
        notificationData: NotificationData,
        context: Context,
    ): NotificationCompat.Style {
        val isHandlerShown = notificationData.notificationHandlerName.isNullOrEmpty().not()
        val isImageShown = notificationData.imageUrl.isNullOrBlank().not()
        val imageIdInt = notificationData.imageId?.toInt()
        val isImageId = imageIdInt != null && imageIdInt != 0

        when {
            isImageShown -> {
                val bitmap = BitmapUtils.getBitmapFromURL(notificationData.imageUrl, context)
                return NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .setBigContentTitle(notificationData.title)
                    .setSummaryText(notificationData.content)
            }
            isImageId -> {
                val personBuilder = Person.Builder()
                personBuilder.setIcon(
                    IconCompat.createWithBitmap(
                        BitmapUtils.drawableToBitmap(
                            context.getDrawableCompact(
                                notificationData.imageId!!.toInt()
                            )
                        )
                    )
                )
                val person = personBuilder
                    .setName(notificationData.title)
                    .setKey(UUID.randomUUID().toString())
                    .build()
                val chatMessageStyle = NotificationCompat.MessagingStyle(person)
                val notificationMessage = NotificationCompat.MessagingStyle.Message(
                    notificationData.content,
                    System.currentTimeMillis(),
                    person
                )
                chatMessageStyle.addMessage(notificationMessage)
                return chatMessageStyle
            }
            isHandlerShown -> {
                val personBuilder = Person.Builder()
                    .setName(notificationData.notificationHandlerName)
                    .setKey(notificationData.notificationHandlerKey)

                if (notificationData.notificationHandlerUrl.isNullOrEmpty().not()) {
                    personBuilder.setIcon(
                        IconCompat.createWithBitmap(
                            BitmapUtils.getBitmapFromURL(notificationData.notificationHandlerUrl, context)
                                .convertToCircle()
                        )
                    )
                } else {
                    val defaultPic = TextDrawable.builder()
                        .buildRound(
                            notificationData.notificationHandlerName?.substring(0, 1)?.toUpperCase(),
                            ColorGenerator.MATERIAL.getColor(notificationData.notificationHandlerName)
                        )
                    personBuilder.setIcon(
                        IconCompat.createWithBitmap(BitmapUtils.textDrawableToBitmap(defaultPic))
                    )
                }

                val person = personBuilder.build()

                val chatMessageStyle = NotificationCompat.MessagingStyle(person)
                val notificationMessage =
                    NotificationCompat.MessagingStyle.Message(
                        notificationData.content,
                        System.currentTimeMillis(),
                        person
                    )
                chatMessageStyle.addMessage(notificationMessage)

                return chatMessageStyle
            }
            else -> {
                return NotificationCompat.BigTextStyle()
                    .setBigContentTitle(notificationData.title)
                    .bigText(notificationData.content)
            }
        }
    }

    private fun reGenerateNewNotificationDatasFromExistingNotifications(
        notificationData: NotificationData,
        currentShowingNotifications: MutableList<NotificationDataWrapper>,
    ) {
        var isExist = false

        for (existingNotification in currentShowingNotifications) {
            if (existingNotification.notificationData.notificationHandlerKey.isNullOrEmpty().not() &&
                existingNotification.notificationData.notificationHandlerKey.equals(notificationData.notificationHandlerKey)
            ) {
                isExist = true
                val modifiedNotificationData =
                    existingNotification.notificationData.copy(
                        content = existingNotification.notificationData.content + "\n" + notificationData.content.toString(),
                        receiveTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
                    )
                currentShowingNotifications.remove(existingNotification)
                currentShowingNotifications.add(
                    NotificationDataWrapper(
                        existingNotification.id,
                        modifiedNotificationData
                    )
                )
                break
            }
        }

        if (!isExist) {
            val newNotificationId = NotificationUtils.assignNotificationId()
            currentShowingNotifications.add(NotificationDataWrapper(newNotificationId, notificationData))
        }
    }

    private fun trackNotificationDisplayed(
        communicationTracker: CommunicationTracker,
        notificationData: NotificationData,
    ) {
        val primaryAction = notificationData.primaryAction ?: ""
        val campaignId = notificationData.campaignId ?: ""
        val subCampaignId = notificationData.subCampaignId ?: ""
        val segment = notificationData.segment ?: ""
        communicationTracker.trackNotificationDisplayed(primaryAction, campaignId, subCampaignId, segment)

        communicationTracker.debugNotification(
            "Step_6_Render",
            notificationData.notificationId,
            notificationData.campaignId,
            notificationData.subCampaignId,
            notificationData.toString()
        )
    }
}
