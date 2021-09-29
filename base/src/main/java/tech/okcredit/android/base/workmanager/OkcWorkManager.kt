package tech.okcredit.android.base.workmanager

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.google.common.util.concurrent.ListenableFuture
import dagger.Lazy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.DateTimeUtils
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * [OkcWorkManager] is a wrapper class on top of android's WorkManager.
 * It basically manipulates the uniqueWorkName passed for OneTimeWork to support [Scope] (for multiple accounts).
 *
 * PS: Do not use WorkManager anywhere in the app.
 */
@AppScope
class OkcWorkManager @Inject constructor(
    context: Context,
    private val preferences: Lazy<WorkManagerPrefs>,
) {

    private val workManager by lazy { WorkManager.getInstance(context) }

    fun schedule(
        uniqueWorkName: String,
        scope: Scope,
        existingWorkPolicy: ExistingWorkPolicy,
        workRequest: OneTimeWorkRequest,
    ) {
        val scopedWorkName = Scope.getScopedKey(uniqueWorkName, scope)
        workManager.beginUniqueWork(scopedWorkName, existingWorkPolicy, workRequest).enqueue()
    }

    fun cancelAllWorkByTag(tag: String) = workManager.cancelAllWorkByTag(tag)

    fun cancelUniqueWork(uniqueWorkName: String, scope: Scope): Operation {
        val scopedWorkName = Scope.getScopedKey(uniqueWorkName, scope)
        return workManager.cancelUniqueWork(scopedWorkName)
    }

    fun getWorkInfoById(id: UUID) = workManager.getWorkInfoById(id)

    fun getWorkInfoByIdLiveData(id: UUID) = workManager.getWorkInfoByIdLiveData(id)

    fun getWorkInfos(workQuery: WorkQuery) = workManager.getWorkInfos(workQuery)

    fun getWorkInfosForUniqueWorkLiveData(uniqueWorkName: String, scope: Scope): LiveData<MutableList<WorkInfo>> {
        val scopedWorkName = Scope.getScopedKey(uniqueWorkName, scope)
        return workManager.getWorkInfosForUniqueWorkLiveData(scopedWorkName)
    }

    fun getWorkInfosForUniqueWork(uniqueWorkName: String, scope: Scope): ListenableFuture<MutableList<WorkInfo>> {
        val scopedWorkName = Scope.getScopedKey(uniqueWorkName, scope)
        return workManager.getWorkInfosForUniqueWork(scopedWorkName)
    }

    /**
     * Function for scheduling one time worker with rate limit.
     * The worker will not be scheduled if the rate limit is not satisfied.
     *
     * PS: Please ensure [uniqueWorkName] is unique across the application,
     * as last scheduled timestamp is stored based on [uniqueWorkName]
     */
    suspend fun scheduleWithRateLimit(
        uniqueWorkName: String,
        scope: Scope,
        existingWorkPolicy: ExistingWorkPolicy,
        workRequest: OneTimeWorkRequest,
        rateLimit: RateLimit,
    ) {
        val scopedWorkName = Scope.getScopedKey(uniqueWorkName, scope)
        val currentTimestampMillis = DateTimeUtils.currentDateTime().millis
        val workerLastTriggeredKey = scopedWorkName.plus(RateLimit.KEY_POSTFIX_LAST_TRIGGERED)

        val canSchedule = checkRateLimit(workerLastTriggeredKey, rateLimit, currentTimestampMillis, scope)
        if (canSchedule) {
            schedule(uniqueWorkName, scope, existingWorkPolicy, workRequest)
            updateLastScheduledTimestamp(workerLastTriggeredKey, currentTimestampMillis, rateLimit, scope)
        } else {
            Timber.d("scheduleWithRateLimit: Skipped scheduling [$uniqueWorkName] due to rate limiting")
        }
    }

    fun scheduleWithRateLimitRx(
        uniqueWorkName: String,
        scope: Scope,
        existingWorkPolicy: ExistingWorkPolicy,
        workRequest: OneTimeWorkRequest,
        rateLimit: RateLimit,
    ) = rxCompletable { scheduleWithRateLimit(uniqueWorkName, scope, existingWorkPolicy, workRequest, rateLimit) }

    private suspend fun checkRateLimit(
        workerLastTriggeredKey: String,
        rateLimit: RateLimit,
        currentTimestampMillis: Long,
        scope: Scope,
    ): Boolean {
        if (rateLimit.limit == 0L) return true

        val lastScheduled = preferences.get().getLong(workerLastTriggeredKey, scope).first()
        return (currentTimestampMillis - lastScheduled) > rateLimit.millis
    }

    private suspend fun updateLastScheduledTimestamp(
        workerLastTriggeredKey: String,
        currentTimestampMillis: Long,
        rateLimit: RateLimit,
        scope: Scope,
    ) = preferences.get().set(workerLastTriggeredKey, currentTimestampMillis, scope)
}
