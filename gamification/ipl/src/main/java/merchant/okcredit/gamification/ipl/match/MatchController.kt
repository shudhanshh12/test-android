package merchant.okcredit.gamification.ipl.match

import android.content.Context
import com.airbnb.epoxy.TypedEpoxyController
import dagger.Lazy
import merchant.okcredit.gamification.ipl.game.data.server.model.response.Match
import merchant.okcredit.gamification.ipl.game.ui.GameRulesCardNew
import merchant.okcredit.gamification.ipl.game.ui.gameRulesCardNew
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.match.views.*
import merchant.okcredit.gamification.ipl.rewards.IplRewardsControllerModel
import javax.inject.Inject

class MatchController @Inject constructor(
    private val context: Lazy<Context>,
    private val eventTracker: Lazy<IplEventTracker>,
    private val retryListener: MatchLoadErrorView.RetryListener,
    private val callback: Lazy<GameRulesCardNew.OnGameRulesListener>,
) :
    TypedEpoxyController<List<MatchControllerModel>>() {

    override fun buildModels(data: List<MatchControllerModel>?) {
        data?.forEach {
            when (it) {
                is MatchControllerModel.ActiveMatchModel -> addMatchView(it.match)
                is MatchControllerModel.NoActiveMatch -> addNoActiveMatchView()
                is MatchControllerModel.MatchLoadError -> addMatchLoadError(it.hasConnectivityIssues)
                is MatchControllerModel.RewardsTitleModel -> addRewardsTitleView()
                is MatchControllerModel.RewardsModel -> addRewardsView(it.rewards)
                is MatchControllerModel.MatchLoadingModel -> addLoader()
                is MatchControllerModel.GameRulesModel -> addGameRulesCard(it.collapsed)
            }
        }
    }

    private fun addMatchView(match: Match) {
        matchView {
            id("matchView$modelCountBuiltSoFar")
            match(match)
            eventTracker(eventTracker.get())
        }
    }

    private fun addNoActiveMatchView() {
        noActiveMatchView {
            id("noActiveMatchView$modelCountBuiltSoFar")
        }
    }

    private fun addMatchLoadError(hasConnectivityIssues: Boolean) {
        matchLoadErrorView {
            id("noActiveMatchView$modelCountBuiltSoFar")
            hasConnectivityIssues(hasConnectivityIssues)
            listener(retryListener)
        }
    }

    private fun addRewardsView(rewards: List<IplRewardsControllerModel>) {
        rewardsView {
            id("rewardsView$modelCountBuiltSoFar")
            rewards(rewards)
            source(IplEventTracker.Value.TODAYS_TAB_SCREEN)
            eventTracker(eventTracker.get())
        }
    }

    private fun addRewardsTitleView() {
        rewardsTitleView {
            id("rewardsTitleView$modelCountBuiltSoFar")
        }
    }

    private fun addLoader() {
        matchLoadingView {
            id("matchLoadingView$modelCountBuiltSoFar")
        }
    }

    private fun addGameRulesCard(collapsed: Boolean) {
        gameRulesCardNew {
            id("leader_board")
            listener(callback.get())
            rulesCollapse(collapsed)
        }
    }
}
