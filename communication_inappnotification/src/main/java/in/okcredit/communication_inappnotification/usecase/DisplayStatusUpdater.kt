package `in`.okcredit.communication_inappnotification.usecase

import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.local.InAppNotificationLocalSource
import `in`.okcredit.communication_inappnotification.local.InAppNotificationPreferences
import `in`.okcredit.communication_inappnotification.local.InAppNotificationPreferences.Keys
import `in`.okcredit.communication_inappnotification.remote.InAppNotificationRemoteSource
import `in`.okcredit.communication_inappnotification.usecase.DisplayStatusUpdater.Worker.Companion.WORK_NAME
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.*
import dagger.Lazy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import org.joda.time.DateTime
import tech.okcredit.android.base.datetime.epoch
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DisplayStatusUpdater @Inject constructor(
    private val localSource: Lazy<InAppNotificationLocalSource>,
    private val remoteSource: Lazy<InAppNotificationRemoteSource>,
    private val preferences: Lazy<InAppNotificationPreferences>,
    private val workManager: Lazy<OkcWorkManager>,
    private val tracker: Lazy<InAppNotificationTracker>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    suspend fun execute(notificationId: String, displayStatus: DisplayStatus, businessId: String? = null) {
        val activeBusinessId = getActiveBusinessId.get().thisOrActiveBusinessId(businessId).await()
        updateStatusOnLocal(notificationId, displayStatus, activeBusinessId)
        scheduleUpdateStatusOnRemote(activeBusinessId)
    }

    private suspend fun updateStatusOnLocal(notificationId: String, displayStatus: DisplayStatus, businessId: String) {
        localSource.get().updateNotificationDisplayStatus(notificationId, displayStatus)
        if (displayStatus == DisplayStatus.DISPLAYED) {
            updateDisplayStatusInPreferences(businessId)
        }
    }

    private suspend fun updateDisplayStatusInPreferences(businessId: String) {
        val date = preferences.get().getLong(Keys.PREF_BUSINESS_DATE, Scope.Business(businessId)).first()
        val currentDayStartTime = DateTime().withTimeAtStartOfDay().epoch

        if (date != currentDayStartTime) {
            preferences.get().set(Keys.PREF_BUSINESS_DATE, currentDayStartTime, Scope.Business(businessId))
        }
        preferences.get().increment(Keys.PREF_BUSINESS_NOTIFICATIONS_SHOWN_FOR_DATE_COUNT, Scope.Business(businessId))
    }

    private fun scheduleUpdateStatusOnRemote(businessId: String) {
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
    internal suspend fun executeUpdateStatusOnRemote(businessId: String) {
        val notificationsToBeSynced = localSource.get().getNotificationsToBeSynced(businessId)
        if (notificationsToBeSynced.isNotEmpty()) {
            val notificationIdsToBeSynced = notificationsToBeSynced.map { it.id }
            val acknowledged = remoteSource.get().acknowledgeNotifications(notificationIdsToBeSynced, businessId)
            if (acknowledged) {
                localSource.get().clearNotifications(notificationIdsToBeSynced)
                trackNotificationAcknowledged(notificationsToBeSynced)
            }
        }
    }

    private fun trackNotificationAcknowledged(notificationsToBeSynced: List<InAppNotification>) {
        notificationsToBeSynced.forEach { notification ->
            tracker.get().trackNotificationAcknowledged(
                type = notification.getTypeForAnalyticsTracking(),
                id = notification.id,
                name = notification.name,
                source = notification.source
            )
        }
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val displayStatusUpdater: Lazy<DisplayStatusUpdater>,
    ) : BaseRxWorker(context, params) {

        companion object {
            const val WORK_NAME = "in-app-notifications/display-status-updater"
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork() = rxCompletable {
            val businessId = inputData.getString(BUSINESS_ID)!!
            displayStatusUpdater.get().executeUpdateStatusOnRemote(businessId)
        }

        class Factory @Inject constructor(private val displayStatusUpdater: Lazy<DisplayStatusUpdater>) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, displayStatusUpdater)
            }
        }
    }
}
