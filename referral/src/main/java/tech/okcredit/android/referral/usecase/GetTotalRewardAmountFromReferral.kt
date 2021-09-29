package tech.okcredit.android.referral.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.rewards.temp.SyncableRewardsRepository
import `in`.okcredit.rewards.contract.RewardsSyncer
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetTotalRewardAmountFromReferral @Inject constructor(
    private val rewardsRepository: Lazy<SyncableRewardsRepository>,
    private val rewardsSyncer: Lazy<RewardsSyncer>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<Unit, GetTotalRewardAmountFromReferral.Response> {

    override fun execute(req: Unit): Observable<Result<Response>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                rewardsSyncer.get().scheduleEverything(businessId).andThen(
                    rewardsRepository.get().listRewards().map { it ->
                        var totalClaimedReferralReward = 0L
                        var totalUnClaimedReferralReward = 0L
                        it.map {
                            if (it.reward_type == "internal_referral_reward") {
                                if (it.isUnclaimed()) {
                                    totalUnClaimedReferralReward += it.amount
                                } else {
                                    totalClaimedReferralReward += it.amount
                                }
                            }
                        }
                        Response(totalClaimedReferralReward, totalUnClaimedReferralReward)
                    }
                )
            }
        )
    }

    data class Response(
        val totalClaimedReferralReward: Long,
        val totalUnClaimedReferralReward: Long,
    )
}
