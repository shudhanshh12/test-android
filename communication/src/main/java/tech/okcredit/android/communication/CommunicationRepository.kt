package tech.okcredit.android.communication

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.utils.DateTimeUtils
import java.util.concurrent.TimeUnit

interface CommunicationRepository {
    fun createNotificationChannel(channelOkC: OkCNotificationChannel): OkCNotificationChannel

    fun scheduleSyncNotification(msg: RemoteMessage)

    fun scheduleProcessingOkCreditNotification(dataString: String)

    fun executeOkCreditNotification(notificationData: NotificationData): Completable

    fun executeSyncNotification(notificationData: NotificationData, businessId: String): Completable

    fun sendDailyReportNotification()

    fun getIntentFromPrimaryAction(action: String): PendingIntent

    fun getApplicationShareReceiverIntent(
        sendIntent: Intent,
        shareType: String,
        contentType: String?
    ): PendingIntent

    fun goToWhatsApp(shareIntentBuilder: ShareIntentBuilder): Single<Intent>

    fun goToSharableApp(shareIntentBuilder: ShareIntentBuilder): Single<Intent>

    fun goToSms(shareIntentBuilder: ShareIntentBuilder): Single<Intent>

    fun clearAllNotifications(): Completable

    fun goToWhatsAppWithTextOnlyExtendedBahaviuor(shareIntentBuilder: ShareIntentBuilder): Single<Intent>
}

data class ShareIntentBuilder(
    val shareText: String? = null,
    var uri: Uri? = null,
    var phoneNumber: String? = null,
    var contentType: String? = null,
    val imageFrom: ImagePath? = null,
    var uriType: String = "imageLocal/jpeg"
)

data class NotificationData(
    @SerializedName("title") val title: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("image_id") val imageId: String? = null,
    @SerializedName("primary_action") val primaryAction: String? = null,
    @SerializedName("btn_primary_label") val btnPrimaryLabel: String? = null,
    @SerializedName("btn_secondary_label") val btnSecondaryLabel: String? = null,
    @SerializedName("btn_primary_action") val btnPrimaryAction: String? = null,
    @SerializedName("btn_secondary_action") val btnSecondaryAction: String? = null,
    @SerializedName("notification_id") val notificationId: String? = null,
    @SerializedName("visible") val visible: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("amount") val amount: String? = null,
    @SerializedName("render_type") val renderType: String? = null,
    @SerializedName("customer_id") val customerId: String? = null,
    @SerializedName("transaction_id") val transactionId: String? = null,
    @SerializedName("business_id") val businessId: String? = null,
    @SerializedName("supplier_id") val supplierId: String? = null,
    @SerializedName("_campaign_id") val campaignId: String? = null,
    @SerializedName("segment") val segment: String? = null,
    @SerializedName("expire_time") val expireTime: String? = null,
    @SerializedName("receive_time") val receiveTime: String? = null,
    @SerializedName("_subcampaign_id") val subCampaignId: String? = null,
    @SerializedName("a_id") val aId: String? = null,
    @SerializedName("notification_handler_key") val notificationHandlerKey: String? = null,
    @SerializedName("notification_handler_name") val notificationHandlerName: String? = null,
    @SerializedName("notification_handler_url") val notificationHandlerUrl: String? = null,
    // added for collection sync handling
    @SerializedName("collection_id") val collectionId: String? = null, // 9RKMDLDB
    @SerializedName("create_time") val createTime: String? = null, // 1603215362
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("payment_id") val paymentId: String? = null, // pay_FrEwddlcMYqf8V
    @SerializedName("payout_id") val payoutId: String? = null, // pay_FrEwddlcMYqf8V
    @SerializedName("payment_type") val paymentType: String? = null, // pay_FrEwddlcMYqf8V
    @SerializedName("status") val status: String? = null, // 1,2,3
    @SerializedName("update_time") val updateTime: String? = null, // 1603215376
) {

    fun getBooleanVisible() = visible == "true"

    fun getExpiryTime(): Long {
        return expireTime?.toLong()
            ?: TimeUnit.MILLISECONDS.toSeconds(DateTimeUtils.currentDateTime().plusDays(1).millis)
    }

    fun getReceivedTime(): Long {
        return receiveTime?.toLong()
            ?: TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    }

    fun getContentTitle(): String? {
        return if (notificationHandlerKey == null) {
            title
        } else {
            notificationHandlerName
        }
    }

    companion object {
        fun from(dataString: String): NotificationData {
            return Gson().fromJson(dataString, NotificationData::class.java)
        }

        const val KEY_NOTIFICATION_INTENT_EXTRA = "data"
        const val KEY_NOTIFICATION_IS_SUMMERY = "is_summery"
    }
}

@Keep
data class OkCNotificationChannel(
    val channelId: String,
    val name: String,
    val descriptionText: String,
    val importance: Int = NotificationManager.IMPORTANCE_HIGH,
    val channelLockScreenVisibility: Int,
    val enableVibrate: Boolean
)

@Keep
data class NotificationPersonProfile(
    val id: String,
    val name: String?,
    val profile_url: String?
)

@Keep
data class NotificationDataWrapper(
    val id: Int,
    val notificationData: NotificationData
)

@Keep
data class DailyReportResponce(
    val netBalance: Long,
    val netCredit: Long,
    val netPayment: Long
)

val PROMOTIONAL_NOTIFICATION_CHANNEL = OkCNotificationChannel(
    "CleverTapNotificationChannel",
    "Promotional",
    "Promotional Notifications",
    NotificationManager.IMPORTANCE_HIGH,
    NotificationCompat.VISIBILITY_PUBLIC,
    true
)

val TRANSACTIONS_NOTIFICATION_CHANNEL = OkCNotificationChannel(
    "business",
    "Transactions",
    "Transactional Notifications",
    NotificationManager.IMPORTANCE_HIGH,
    NotificationCompat.VISIBILITY_PUBLIC,
    true
)

val SYNC_PROGRESS_NOTIFICATION_CHANNEL = OkCNotificationChannel(
    "sync_progress",
    "Sync Progress",
    "Sync Notification Progress",
    NotificationManager.IMPORTANCE_DEFAULT,
    NotificationCompat.VISIBILITY_PUBLIC,
    false
)

enum class PreDefinedAction(val value: String) {
    DISMISS("/dismiss"),
    MARK_AS_READ("/mark_as_read")
}

enum class PreDefinedNotificationRenderStyle(val value: String) {
    DAILY_REPORT("daily_report"),
}

object CommunicationConstants {
    internal const val GROUP_SUMMREY_KEY = "transactional"
    const val APPS_FLYER_UNINSTALL_NOTIFICATION = "af-uinstall-tracking"

    internal val PRIMARY_ACTION_DAILY_REPORT =
        if (BuildConfig.DEBUG) "https://staging.okcredit.app/merchant/v1/home" else "https://okcredit.app/merchant/v1/home"
}

/** Phase-2 **/

// Add Intent for whatsApp ans Sms communication in the App
// Clean API
// Document Everything
// Write UnitTests.

/** Testing **/
// Test Api calls.
// Test on all Android API Versions.
// Test Clevertap and Netcore notification
