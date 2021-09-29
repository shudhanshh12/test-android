package merchant.okcredit.gamification.ipl.sundaygame.epoxy.controller

import merchant.okcredit.gamification.ipl.rewards.IplRewardsControllerModel

sealed class SundayGameModel {
    data class GameRules(
        val boosterCount: Int,
        val totalRuns: Int,
        val date: Long,
        val rulesCollapsed: Boolean,
    ) : SundayGameModel()

    data class Rewards(
        val rewards: List<IplRewardsControllerModel>,
    ) : SundayGameModel()

    object PlayAgain : SundayGameModel()

    data class PendingBoosterCard(
        val boosterState: BoosterSate,
        val endTime: Int,
        val pendingRuns: Int = 0,
        val runs: Int = 0,
        val threadHoldBooster: Int = 0,
        val threadHoldRuns: Int = 0,
        val isRunsCompleted: Boolean = false,
    ) : SundayGameModel()

    data class CompletedBoosterCard(
        val cardNumber: Int,
        val totalRuns: Int? = null,
    ) : SundayGameModel()

    data class LuckyDrawQualifiedCard(
        val runs: Int = 0,
        val endTime: Int,
        val date: Long,
    ) : SundayGameModel()

    enum class BoosterSate(var boosterState: String) {
        NO_BOOSTER_DONE("NO_BOOSTER_DONE"),
        BOOSTER_IN_PROGRESS("NO_BOOSTER_DONE"),
        BOOSTER_COMPLETED("BOOSTER_COMPLETED"),
    }
}
