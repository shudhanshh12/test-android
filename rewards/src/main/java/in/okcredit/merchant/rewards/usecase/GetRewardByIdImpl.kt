package `in`.okcredit.merchant.rewards.usecase

import `in`.okcredit.rewards.contract.GetRewardById
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardsRepository
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetRewardByIdImpl @Inject constructor(
    private val rewardsRepository: Lazy<RewardsRepository>,
) : GetRewardById {

    override fun execute(id: String): Observable<RewardModel> {
        return rewardsRepository.get().getRewardById(id)
    }
}
