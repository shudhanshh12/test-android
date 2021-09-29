package `in`.okcredit.communication_inappnotification.usecase

import `in`.okcredit.communication_inappnotification.contract.InAppNotificationRepository
import `in`.okcredit.communication_inappnotification.local.InAppNotificationLocalSource
import `in`.okcredit.communication_inappnotification.local.InAppNotificationPreferences
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class InAppNotificationRepositoryImpl @Inject constructor(
    private val localSource: Lazy<InAppNotificationLocalSource>,
    private val preferences: Lazy<InAppNotificationPreferences>,
    private val syncer: Lazy<InAppNotificationsSyncer>
) : InAppNotificationRepository {

    override fun scheduleSyncCompletable(businessId: String) = Completable.fromAction {
        syncer.get().schedule(businessId)
    }

    override suspend fun scheduleSync(businessId: String) = syncer.get().schedule(businessId)

    override fun clear() = rxCompletable {
        localSource.get().clear()
        preferences.get().clear()
    }
}
