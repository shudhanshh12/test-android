package `in`.okcredit.merchant.rewards.ui.rewards_screen.usecase

import `in`.okcredit.merchant.rewards.temp.SyncableRewardsRepository
import `in`.okcredit.rewards.contract.RewardModel
import io.reactivex.Observable
import javax.inject.Inject

class GetAllRewards @Inject constructor(
    private val rewardsRepository: SyncableRewardsRepository,
) {

    fun execute(): Observable<RewardsStatement> {
        return rewardsRepository.listRewards().flatMap { listRewards ->
            var sumOfClaimedRewards = 0L
            var unclaimedRewards = 0L
            listRewards.forEach {
                if (it.isUnclaimed()) {
                    unclaimedRewards += it.amount
                } else {
                    sumOfClaimedRewards += it.amount
                }
            }
            return@flatMap Observable.just(
                RewardsStatement(
                    listRewards,
                    sumOfClaimedRewards,
                    unclaimedRewards
                )
            )
        }
    }

    data class RewardsStatement(
        val rewards: List<RewardModel>,
        val sumOfClaimedRewards: Long,
        val unclaimedRewards: Long,
    )
}
