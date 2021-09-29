package `in`.okcredit.merchant.rewards.ui.rewards_screen

import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardType
import java.util.concurrent.TimeUnit

sealed class RewardsControllerModel constructor(val isClaimed: Boolean, val createdAt: Long) {

    data class ClaimedReward(val reward: RewardModel) :
        RewardsControllerModel(reward.isClaimed(), TimeUnit.MILLISECONDS.toSeconds(reward.create_time.millis))

    data class UnclaimedReward(val reward: RewardModel) :
        RewardsControllerModel(reward.isClaimed(), TimeUnit.MILLISECONDS.toSeconds(reward.create_time.millis))

    data class ClaimedReferralRewards(val reward: RewardModel) :
        RewardsControllerModel(reward.isClaimed(), TimeUnit.MILLISECONDS.toSeconds(reward.create_time.millis))

    data class UnclaimedReferralRewards(val reward: RewardModel) :
        RewardsControllerModel(reward.isClaimed(), TimeUnit.MILLISECONDS.toSeconds(reward.create_time.millis))

    data class ClaimedActivationRewards(val reward: RewardModel) :
        RewardsControllerModel(reward.isClaimed(), TimeUnit.MILLISECONDS.toSeconds(reward.create_time.millis))

    data class UnclaimedActivationRewards(val reward: RewardModel) :
        RewardsControllerModel(reward.isClaimed(), TimeUnit.MILLISECONDS.toSeconds(reward.create_time.millis))
}

fun RewardModel.toControllerModel(): RewardsControllerModel {
    return if (this.isClaimed()) {
        when (RewardType.fromString(this.reward_type ?: "")) {
            RewardType.ACTIVATION_FEATURE_REWARDS,
            RewardType.ACTIVATION_MONEY_REWARDS -> RewardsControllerModel.ClaimedActivationRewards(this)
            RewardType.REFERRAL_REWARDS -> RewardsControllerModel.ClaimedReferralRewards(this)
            else -> RewardsControllerModel.ClaimedReward(this)
        }
    } else {
        when (RewardType.fromString(this.reward_type ?: "")) {
            RewardType.ACTIVATION_FEATURE_REWARDS,
            RewardType.ACTIVATION_MONEY_REWARDS -> RewardsControllerModel.UnclaimedActivationRewards(this)
            RewardType.REFERRAL_REWARDS -> RewardsControllerModel.UnclaimedReferralRewards(this)
            else -> RewardsControllerModel.UnclaimedReward(this)
        }
    }
}
