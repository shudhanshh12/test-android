package `in`.okcredit.communication_inappnotification.contract

import io.reactivex.Completable

interface InAppNotificationRepository {
    fun scheduleSyncCompletable(businessId: String): Completable
    suspend fun scheduleSync(businessId: String)
    fun clear(): Completable
}
