package tech.okcredit.home.usecase

import `in`.okcredit.merchant.rewards.temp.SyncableRewardsRepository
import `in`.okcredit.rewards.contract.RewardModel
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetUnClaimedRewards @Inject constructor(
    private val rewardsRepository: Lazy<SyncableRewardsRepository>,
) {

    fun execute(): Single<List<RewardModel>> {
        return rewardsRepository.get().listRewards().firstOrError().map { it ->
            it.filter { it.isUnclaimed() }
        }
    }
}
