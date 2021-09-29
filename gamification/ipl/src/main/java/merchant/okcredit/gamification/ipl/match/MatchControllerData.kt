package merchant.okcredit.gamification.ipl.match

import merchant.okcredit.gamification.ipl.match.views.MatchControllerModel
import merchant.okcredit.gamification.ipl.rewards.IplRewardsControllerModel

data class MatchControllerData(
    val matchModels: List<MatchControllerModel> = listOf(),
    val rewardModels: List<IplRewardsControllerModel> = listOf(),
)
