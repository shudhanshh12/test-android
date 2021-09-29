package `in`.okcredit.rewards.contract

import io.reactivex.Completable
import io.reactivex.Observable
import org.joda.time.DateTime

interface RewardsSyncer {
    // Todo(discuss with saket) Sync Reward should be Observable
    suspend fun syncRewards(businessId: String? = null): List<RewardModel>

    fun getLastSyncEverythingTime(): Observable<Pair<Boolean, DateTime?>>

    suspend fun setLastRewardsSyncTime(time: DateTime)

    fun scheduleEverything(businessId: String): Completable
}
