package merchant.okcredit.gamification.ipl.match.usecase

import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardType
import `in`.okcredit.rewards.contract.RewardsRepository
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetAllIplRewards @Inject constructor(private val rewardsRepository: Lazy<RewardsRepository>) {

    fun execute(): Observable<Result<List<RewardModel>>> {
        return UseCase.wrapObservable(
            rewardsRepository.get().getRewards(RewardType.IPL_REWARDS).map {
                // Exclude claimed better luck next time rewards
                it.filter { reward -> !(reward.isClaimed() && reward.amount == 0L) }
            }
        )
    }
}
