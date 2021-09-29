package tech.okcredit.android.communication

import io.reactivex.Completable

interface GetSyncNotificationJobBinding {
    fun getSyncNotificationJobBinding(data: NotificationData, businessId: String): Completable
}
