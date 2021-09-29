package `in`.okcredit.communication_inappnotification.local

import `in`.okcredit.communication_inappnotification._di.CommunicationInAppNotification
import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.Lazy
import javax.inject.Inject
import `in`.okcredit.communication_inappnotification.local.InAppNotification as DbInAppNotification

class InAppNotificationLocalSourceImpl @Inject constructor(
    private val dao: Lazy<InAppNotificationDatabaseDao>,
    @CommunicationInAppNotification private val moshi: Lazy<Moshi>
) : InAppNotificationLocalSource {

    private val adapter: JsonAdapter<InAppNotification> = moshi.get().adapter(InAppNotification::class.java)

    override suspend fun replaceNotifications(notifications: List<InAppNotification>, businessId: String) {
        val dbNotifications = notifications.map { it.toDbInAppNotification(businessId) }
        val notificationArray = dbNotifications.toTypedArray()
        dao.get().replaceNotifications(notificationArray, businessId)
    }

    override suspend fun getNotificationsNotDisplayedForScreen(
        screenName: String,
        businessId: String,
    ): List<InAppNotification> {
        return dao.get().getNotificationsNotDisplayedForScreen(screenName, DisplayStatus.DISPLAYED, businessId)
            .mapNotNull { it.toInAppNotification() }
    }

    override suspend fun updateNotificationDisplayStatus(notificationId: String, displayStatus: DisplayStatus) {
        dao.get().updateNotificationDisplayStatus(notificationId, displayStatus)
    }

    override suspend fun getNotificationsToBeSynced(businessId: String): List<InAppNotification> {
        return dao.get().getNotificationsToBeSynced(DisplayStatus.DISPLAYED, businessId)
            .mapNotNull { it.toInAppNotification() }
    }

    override suspend fun getAllNotifications(businessId: String): List<InAppNotification> {
        return dao.get().getAllNotifications(businessId)
            .mapNotNull { it.toInAppNotification() }
    }

    override suspend fun clearNotifications(notificationIds: List<String>) {
        return dao.get().clearNotifications(notificationIds.toTypedArray())
    }

    override suspend fun clear() {
        return dao.get().clearNotificationTable()
    }

    private fun InAppNotification.toDbInAppNotification(businessId: String) = DbInAppNotification(
        id = this.id,
        screenName = this.screenName,
        notificationJson = adapter.toJson(this),
        displayStatus = this.displayStatus,
        businessId = businessId
    )

    private fun DbInAppNotification.toInAppNotification() = adapter.fromJson(this.notificationJson)
}
