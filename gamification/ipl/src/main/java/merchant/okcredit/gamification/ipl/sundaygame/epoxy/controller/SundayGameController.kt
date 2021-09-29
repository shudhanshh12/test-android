package merchant.okcredit.gamification.ipl.sundaygame.epoxy.controller

import android.content.Context
import com.airbnb.epoxy.TypedEpoxyController
import dagger.Lazy
import merchant.okcredit.gamification.ipl.game.ui.GameRulesCardWeekly
import merchant.okcredit.gamification.ipl.game.ui.gameRulesCardWeekly
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.match.views.rewardsView
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.view.luckyDrawQualifiedNew
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.view.pendingBoosterNew
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.view.playAgainCard
import javax.inject.Inject

class SundayGameController @Inject constructor(
    private val context: Lazy<Context>,
    private val eventTracker: Lazy<IplEventTracker>,
    private val callback: Lazy<GameRulesCardWeekly.OnGameRulesListener>,
) : TypedEpoxyController<List<SundayGameModel>>() {

    override fun buildModels(data: List<SundayGameModel>?) {
        data?.forEach {
            when (it) {
                is SundayGameModel.Rewards -> {
                    rewardsView {
                        id("rewardsView$modelCountBuiltSoFar")
                        rewards(it.rewards)
                        source(IplEventTracker.Value.SUNDAY_TAB_SCREEN)
                        eventTracker(eventTracker.get())
                    }
                }
                is SundayGameModel.PlayAgain -> {
                    playAgainCard {
                        id("play_again_card")
                    }
                }
                is SundayGameModel.LuckyDrawQualifiedCard -> {
                    luckyDrawQualifiedNew {
                        id("luckyDrawQualified$modelCountBuiltSoFar")
                        luckyDrawDate(it)
                    }
                }
                is SundayGameModel.PendingBoosterCard -> {
                    pendingBoosterNew {
                        id("pendingBooster$modelCountBuiltSoFar")
                        cardDetails(it)
                    }
                }
                is SundayGameModel.GameRules -> {
                    gameRulesCardWeekly {
                        id("gameRulesCard$modelCountBuiltSoFar")
                        rulesData(it)
                        rulesCollapse(it.rulesCollapsed)
                        listener(callback.get())
                    }
                }
            }
        }
    }
}
