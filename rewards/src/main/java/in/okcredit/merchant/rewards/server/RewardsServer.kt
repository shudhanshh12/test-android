package `in`.okcredit.merchant.rewards.server

import `in`.okcredit.merchant.rewards.server.internal.ApiMessages.*
import `in`.okcredit.merchant.rewards.server.internal.RewardsApiClient
import `in`.okcredit.merchant.rewards.server.internal.toRewardModel
import `in`.okcredit.rewards.contract.RewardModel
import dagger.Lazy
import dagger.Reusable
import javax.inject.Inject

@Reusable
class RewardsServer @Inject constructor(
    private val rewardsApiClient: Lazy<RewardsApiClient>,
) {

    suspend fun getRewards(businessId: String): List<RewardModel> = rewardsApiClient.get()
        .listRewards(ListRewardApiRequest, businessId)
        .run { rewards.map { it.toRewardModel() } }

    suspend fun claimReward(rewardId: String, locale: String, businessId: String): ClaimRewardResponse =
        rewardsApiClient.get()
            .claimReward(ClaimRewardRequest(rewardId, locale), businessId)
}
