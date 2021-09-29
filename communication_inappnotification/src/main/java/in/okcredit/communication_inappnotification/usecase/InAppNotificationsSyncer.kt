package `in`.okcredit.communication_inappnotification.usecase

import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.local.InAppNotificationLocalSource
import `in`.okcredit.communication_inappnotification.remote.InAppNotificationRemoteSource
import `in`.okcredit.communication_inappnotification.usecase.InAppNotificationsSyncer.Worker.Companion.WORK_NAME
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.*
import dagger.Lazy
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InAppNotificationsSyncer @Inject constructor(
    private val remoteSource: Lazy<InAppNotificationRemoteSource>,
    private val localSource: Lazy<InAppNotificationLocalSource>,
    private val workManager: Lazy<OkcWorkManager>,
    private val tracker: Lazy<InAppNotificationTracker>,
) {

    fun schedule(businessId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequest.Builder(Worker::class.java)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .addTag(WORK_NAME)
            .setInputData(
                workDataOf(
                    Worker.BUSINESS_ID to businessId
                )
            )
            .setConstraints(constraints)
            .build()
            .enableWorkerLogging()

        workManager.get()
            .schedule(WORK_NAME, Scope.Business(businessId), ExistingWorkPolicy.KEEP, request)
    }

    @VisibleForTesting
    internal suspend fun execute(businessId: String) {
        val notificationsReceivedFromRemote = remoteSource.get().getNotifications(businessId)
        val notificationsPresentInLocal = localSource.get().getAllNotifications(businessId)
        val newNotificationsReceived = filterNewNotificationsReceived(
            notificationsReceivedFromRemote = notificationsReceivedFromRemote,
            notificationsPresentInLocal = notificationsPresentInLocal
        )
        localSource.get().replaceNotifications(notificationsReceivedFromRemote, businessId)
        trackNotificationsReceived(newNotificationsReceived)
    }

    private fun trackNotificationsReceived(notifications: List<InAppNotification>) {
        notifications.forEach { notification ->
            tracker.get().trackNotificationReceived(
                type = notification.getTypeForAnalyticsTracking(),
                id = notification.id,
                name = notification.name,
                source = notification.source
            )
        }
    }

    private fun filterNewNotificationsReceived(
        notificationsReceivedFromRemote: List<InAppNotification>,
        notificationsPresentInLocal: List<InAppNotification>
    ): List<InAppNotification> {
        val notificationIdsPresentInLocal = notificationsPresentInLocal.map { it.id }
        return notificationsReceivedFromRemote.filter { it.id !in notificationIdsPresentInLocal }
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<InAppNotificationsSyncer>
    ) : BaseRxWorker(context, params) {

        companion object {
            const val WORK_NAME = "in-app-notifications/sync-worker"
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork() = rxCompletable {
            val businessId = inputData.getString(BUSINESS_ID)!!
            syncer.get().execute(businessId)
        }

        class Factory @Inject constructor(private val syncer: Lazy<InAppNotificationsSyncer>) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncer)
            }
        }
    }
}
