package `in`.okcredit.merchant.rewards

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.rewards.RewardsRepositoryImpl.Companion.TAG
import `in`.okcredit.merchant.rewards.server.RewardsServer
import `in`.okcredit.merchant.rewards.store.RewardsStore
import `in`.okcredit.merchant.rewards.utils.CommonUtils
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardsSyncer
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import timber.log.Timber
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/********** All sync rewards implementation here **********/
class RewardsSyncerImpl @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val store: Lazy<RewardsStore>,
    private val server: Lazy<RewardsServer>,
    schedulerProvider: Lazy<SchedulerProvider>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : RewardsSyncer {

    companion object {
        const val WORKER_TAG_BASE = "rewards"
        const val WORKER_TAG_SYNC_EVERYTHING = "rewards/scheduleEverything"
    }

    init {
        Timber.i("$TAG init SyncerImpl Rewards")
        getLastSyncEverythingTime().subscribeOn(schedulerProvider.get().io()).subscribe()
    }

    /********** Everything **********/
    override fun scheduleEverything(businessId: String): Completable {
        Timber.i("$TAG scheduleEverything Scheduling")
        return Completable.fromAction {
            val workName = WORKER_TAG_SYNC_EVERYTHING

            val workRequest = OneTimeWorkRequestBuilder<SyncEverythingWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    workDataOf(
                        SyncEverythingWorker.BUSINESS_ID to businessId
                    )
                )
                .addTag(WORKER_TAG_BASE)
                .addTag(WORKER_TAG_SYNC_EVERYTHING)
                .addTag(workName)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(workName, Scope.Individual, ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    class SyncEverythingWorker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<RewardsSyncer>,
    ) : CoroutineWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business-id"
        }

        override suspend fun doWork(): Result = syncRewards()

        private suspend fun syncRewards(): Result = withContext(Dispatchers.IO) {
            Timber.i("$TAG scheduleEverything executing")
            val businessId = inputData.getString(BUSINESS_ID)
            try {
                val startTime = CommonUtils.currentDateTime()
                syncer.get().syncRewards(businessId)
                syncer.get().setLastRewardsSyncTime(startTime)
                Result.success()
            } catch (c: CancellationException) {
                Result.retry()
            } catch (e: Exception) {
                Result.failure()
            }
        }

        class Factory @Inject constructor(private val syncer: Lazy<RewardsSyncer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncEverythingWorker(context, params, syncer)
            }
        }
    }

    override suspend fun syncRewards(businessId: String?): List<RewardModel> {
        val mBusinessId = getActiveBusinessId.get().thisOrActiveBusinessId(businessId).await()
        return server.get()
            .getRewards(mBusinessId).also {
                Timber.i("$TAG server response rewards count=${it.size}")
                if (it.isNotEmpty()) {
                    store.get().putRewards(it)
                }
            }
    }

    // Getting Last sync time of rewards
    override fun getLastSyncEverythingTime(): Observable<Pair<Boolean, DateTime?>> {
        return store.get().getLastSyncEverythingTime()
            .doOnNext { Timber.i("$TAG executed getLastSyncEverythingTime value=$it") }
    }

    // Updating Last sync time of rewards
    override suspend fun setLastRewardsSyncTime(time: DateTime) {
        store.get().setLastSyncEverythingTime(time)
    }
}
