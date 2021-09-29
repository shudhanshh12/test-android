package merchant.okcredit.gamification.ipl.sundaygame.usecase

import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardType
import `in`.okcredit.rewards.contract.RewardsRepository
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.gamification.ipl.utils.IplUtils
import javax.inject.Inject

class GetWeeklyIplRewards @Inject constructor(private val rewardsRepository: Lazy<RewardsRepository>) {

    fun execute(): Observable<Result<List<RewardModel>>> {
        return UseCase.wrapObservable(
            rewardsRepository.get()
                .getRewards(RewardType.IPL_ALL_WEEKLY)
                .map {

                    it.filter { reward ->
                        IplUtils.isThisWeeksReward(reward.create_time.millis) &&
                            !(reward.isClaimed() && reward.amount == 0L)
                    }
                }
        )
    }
}
