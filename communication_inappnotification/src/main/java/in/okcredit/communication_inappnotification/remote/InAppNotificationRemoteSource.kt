package `in`.okcredit.communication_inappnotification.remote

import `in`.okcredit.communication_inappnotification.contract.InAppNotification

interface InAppNotificationRemoteSource {

    suspend fun getNotifications(businessId: String): List<InAppNotification>

    suspend fun acknowledgeNotifications(ids: List<String>, businessId: String): Boolean
}
