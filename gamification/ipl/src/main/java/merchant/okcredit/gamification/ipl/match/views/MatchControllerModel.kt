package merchant.okcredit.gamification.ipl.match.views

import merchant.okcredit.gamification.ipl.game.data.server.model.response.Match
import merchant.okcredit.gamification.ipl.rewards.IplRewardsControllerModel

sealed class MatchControllerModel {

    data class ActiveMatchModel(val match: Match) : MatchControllerModel()

    data class MatchLoadError(val hasConnectivityIssues: Boolean = true) : MatchControllerModel()

    object MatchLoadingModel : MatchControllerModel()

    object RewardsTitleModel : MatchControllerModel()

    data class GameRulesModel(val collapsed: Boolean) : MatchControllerModel()

    data class RewardsModel(val rewards: List<IplRewardsControllerModel> = listOf()) : MatchControllerModel()

    object NoActiveMatch : MatchControllerModel()
}
