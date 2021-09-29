package merchant.okcredit.gamification.ipl.rewards

import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardType
import merchant.okcredit.gamification.ipl.game.data.server.model.response.MysteryPrizeModel
import java.util.concurrent.TimeUnit

sealed class IplRewardsControllerModel constructor(val isClaimed: Boolean, val createdAt: Long) {

    data class ClaimedCashReward(val reward: RewardModel) :
        IplRewardsControllerModel(reward.isClaimed(), TimeUnit.MILLISECONDS.toSeconds(reward.create_time.millis))

    data class ClaimedBatReward(val reward: RewardModel) :
        IplRewardsControllerModel(reward.isClaimed(), TimeUnit.MILLISECONDS.toSeconds(reward.create_time.millis))

    data class ClaimedTshirtReward(val reward: RewardModel) :
        IplRewardsControllerModel(reward.isClaimed(), TimeUnit.MILLISECONDS.toSeconds(reward.create_time.millis))

    data class UnclaimedReward(val reward: RewardModel) :
        IplRewardsControllerModel(reward.isClaimed(), TimeUnit.MILLISECONDS.toSeconds(reward.create_time.millis))

    data class UnclaimedMysteryPrize(val prize: MysteryPrizeModel) :
        IplRewardsControllerModel(prize.isClaimed(), prize.created)

    data class ClaimedMysteryPrize(val prize: MysteryPrizeModel) :
        IplRewardsControllerModel(prize.isClaimed(), prize.created)
}

fun RewardModel.toControllerModel(): IplRewardsControllerModel {
    return if (this.isClaimed()) {
        when (RewardType.fromString(this.reward_type ?: "")) {
            RewardType.IPL_BAT -> IplRewardsControllerModel.ClaimedBatReward(this)
            RewardType.IPL_TSHIRT -> IplRewardsControllerModel.ClaimedTshirtReward(this)
            else -> IplRewardsControllerModel.ClaimedCashReward(this)
        }
    } else {
        IplRewardsControllerModel.UnclaimedReward(this)
    }
}

fun MysteryPrizeModel.toControllerModel(): IplRewardsControllerModel {
    return if (this.isClaimed()) {
        IplRewardsControllerModel.ClaimedMysteryPrize(this)
    } else {
        IplRewardsControllerModel.UnclaimedMysteryPrize(this)
    }
}
