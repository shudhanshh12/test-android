package `in`.okcredit.merchant.rewards.store

import `in`.okcredit.merchant.rewards.RewardsRepositoryImpl.Companion.TAG
import `in`.okcredit.merchant.rewards.store.database.RewardEntityMapper
import `in`.okcredit.merchant.rewards.store.database.Rewards
import `in`.okcredit.merchant.rewards.store.database.RewardsDataBaseDao
import `in`.okcredit.merchant.rewards.utils.CommonUtils
import `in`.okcredit.merchant.rewards.utils.Utils
import `in`.okcredit.rewards.contract.RewardModel
import android.content.SharedPreferences
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.blockingGetLong
import tech.okcredit.android.base.preferences.blockingSet
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.offloaded
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RewardsStore @Inject constructor(
    private val rewardsDao: Lazy<RewardsDataBaseDao>,
    private val rewardPreference: Lazy<RewardPreference>,
) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val lastSyncEverythingTime = BehaviorSubject.create<Pair<Boolean, DateTime?>>()

    init {
        Timber.i("$TAG init Store Rewards")
        Completable
            .fromAction {
                rewardPreference.get().registerOnSharedPreferenceChangeListener(this)
                // set value initially
                updateLastSyncEverythingTime()
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .subscribe()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // track further changes
        if (key?.contains(KEY_REWARDS_LAST_SYNC_EVERYTHING_TIME) == true) {
            updateLastSyncEverythingTime()
        }
    }

    private fun updateLastSyncEverythingTime() {
        val time = rewardPreference.get().blockingGetLong(KEY_REWARDS_LAST_SYNC_EVERYTHING_TIME, Scope.Individual)
        if (time == 0L) {
            lastSyncEverythingTime.onNext(false to null)
        } else {
            lastSyncEverythingTime.onNext(true to DateTime(time))
        }
    }

    fun getLastSyncEverythingTime(): Observable<Pair<Boolean, DateTime?>> {
        return lastSyncEverythingTime.hide().distinctUntilChanged()
    }

    suspend fun setLastSyncEverythingTime(time: DateTime?) = withContext(Dispatchers.IO) {
        if (time != null) {
            rewardPreference.get().set(KEY_REWARDS_LAST_SYNC_EVERYTHING_TIME, time.millis, Scope.Individual)
        }
    }

    /********************************* Rewards *********************************/
    fun listRewards(filterOutRewardsType: List<String>): Observable<List<RewardModel>> {
        Timber.i("$TAG executing listRewards from rewardsDao store")
        return rewardsDao.get().listRewards(filterOutRewardsType)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map {
                CommonUtils.mapList(it, RewardEntityMapper.mapper.reverse())
            }
            .toObservable()
    }

    suspend fun putRewards(rewards: List<RewardModel>) {
        Timber.i("$TAG executing putRewards from rewardsDao store")
        val list = Utils.mapList(rewards, RewardEntityMapper.mapper).toTypedArray<Rewards>()
        rewardsDao.get().insertRewards(*list)
    }

    fun clearRewards(): Completable {
        return Completable.fromAction {
            rewardsDao.get().clearRewards()
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun getRewards(types: List<String>): Observable<List<Rewards>> = rewardsDao
        .offloaded()
        .flatMapObservable {
            it.getDistinctRewards(types)
        }

    fun getRewardById(id: String): Observable<RewardModel> {
        return rewardsDao.get().getDistinctRewardById(id)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map {
                RewardEntityMapper.mapper.reverse().convert(it)
            }
    }

    companion object {
        private const val KEY_REWARDS_LAST_SYNC_EVERYTHING_TIME = "KEY_REWARDS_LAST_SYNC_EVERYTHING_TIME_2"
        private const val CLAIM_REWARD_ERROR_PREFERENCES = "claim reward error preferences"
    }

    fun setClaimErrorPreference(status: Boolean) =
        rewardPreference.get().blockingSet(CLAIM_REWARD_ERROR_PREFERENCES, status, Scope.Individual)

    fun getClaimErrorPreference() =
        rewardPreference.get().getBoolean(CLAIM_REWARD_ERROR_PREFERENCES, Scope.Individual).asObservable()
}
