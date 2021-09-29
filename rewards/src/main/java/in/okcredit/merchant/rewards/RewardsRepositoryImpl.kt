package `in`.okcredit.merchant.rewards

import `in`.okcredit.merchant.rewards.server.RewardsServer
import `in`.okcredit.merchant.rewards.store.RewardsStore
import `in`.okcredit.merchant.rewards.store.database.RewardEntityMapper
import `in`.okcredit.merchant.rewards.utils.CommonUtils
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardType
import `in`.okcredit.rewards.contract.RewardsRepository
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import javax.inject.Inject

class RewardsRepositoryImpl @Inject constructor(
    private val store: RewardsStore,
    private val server: RewardsServer,
    private val syncer: RewardsSyncerImpl,
) : RewardsRepository {

    // Single should complete exceptionally if reward missing on sync
    fun claimReward(rewardId: String, userLocale: String, businessId: String) = rxSingle {
        server.claimReward(rewardId, userLocale, businessId)
            .also { syncer.syncRewards() }
    }

    companion object {
        const val TAG = "<<<<RewardsSDK"
    }

    override fun getRewardById(id: String): Observable<RewardModel> {
        return store.getRewardById(id)
    }

    override fun listRewards(): Observable<List<RewardModel>> {
        Timber.i("$TAG listRewards inside store executing")
        return store.listRewards(filterOutRewardsType = RewardType.IPL_REWARDS.map { it.type })
            .observeOn(ThreadUtils.worker())
    }

    override fun listRewardsFromServer(businessId: String): Single<List<RewardModel>> {
        return rxSingle { server.getRewards(businessId) }
    }

    override fun clearLocalData() = store.clearRewards()

    override fun getRewards(rewardTypes: List<RewardType>): Observable<List<RewardModel>> {
        return store.getRewards(rewardTypes.map { it.type }).map { entities ->
            CommonUtils.mapList(entities, RewardEntityMapper.mapper.reverse())
        }
    }

    override fun setClaimErrorPreference(status: Boolean) = store.setClaimErrorPreference(status)

    override fun getClaimErrorPreference(): Observable<Boolean> = store.getClaimErrorPreference()
}
