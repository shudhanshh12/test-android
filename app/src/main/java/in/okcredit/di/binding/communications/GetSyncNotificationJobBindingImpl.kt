package `in`.okcredit.di.binding.communications

import `in`.okcredit.SyncNotificationHandler
import dagger.Lazy
import tech.okcredit.android.communication.GetSyncNotificationJobBinding
import tech.okcredit.android.communication.NotificationData
import javax.inject.Inject

class GetSyncNotificationJobBindingImpl @Inject constructor(private val syncNotificationHandler: Lazy<SyncNotificationHandler>) :
    GetSyncNotificationJobBinding {

    override fun getSyncNotificationJobBinding(data: NotificationData, businessId: String) =
        syncNotificationHandler.get().execute(data, businessId)
}
