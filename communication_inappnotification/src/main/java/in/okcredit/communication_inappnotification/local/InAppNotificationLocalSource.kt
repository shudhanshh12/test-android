package `in`.okcredit.communication_inappnotification.local

import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification

interface InAppNotificationLocalSource {
    suspend fun replaceNotifications(notifications: List<InAppNotification>, businessId: String)
    suspend fun getNotificationsNotDisplayedForScreen(screenName: String, businessId: String): List<InAppNotification>
    suspend fun updateNotificationDisplayStatus(notificationId: String, displayStatus: DisplayStatus)
    suspend fun getNotificationsToBeSynced(businessId: String): List<InAppNotification>
    suspend fun getAllNotifications(businessId: String): List<InAppNotification>
    suspend fun clearNotifications(notificationIds: List<String>)
    suspend fun clear()
}
