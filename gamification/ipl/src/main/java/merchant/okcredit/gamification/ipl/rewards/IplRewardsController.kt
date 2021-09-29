package merchant.okcredit.gamification.ipl.rewards

import com.airbnb.epoxy.TypedEpoxyController
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.claimedMysteryPrizeView

class IplRewardsController constructor(
    var source: String? = null,
    var eventTracker: IplEventTracker? = null,
) : TypedEpoxyController<List<IplRewardsControllerModel>>() {

    override fun buildModels(data: List<IplRewardsControllerModel>?) {
        val sortedData = data?.sortedByDescending { it.createdAt }
        sortedData?.forEach {
            when (it) {
                is IplRewardsControllerModel.ClaimedCashReward -> {
                    claimedCashRewardView {
                        id("claimedIplRewardView$modelCountBuiltSoFar")
                        reward(it.reward)
                        source(this@IplRewardsController::getSource)
                        eventTracker(this@IplRewardsController::getEventTracker)
                    }
                }

                is IplRewardsControllerModel.UnclaimedReward -> {
                    unclaimedIplRewardView {
                        id("unclaimedIplRewardView$modelCountBuiltSoFar")
                        reward(it.reward)
                        source(this@IplRewardsController::getSource)
                        eventTracker(this@IplRewardsController::getEventTracker)
                    }
                }

                is IplRewardsControllerModel.ClaimedBatReward -> {
                    claimedBatRewardView {
                        id("claimedBatRewardView$modelCountBuiltSoFar")
                        reward(it.reward)
                        source(this@IplRewardsController::getSource)
                        eventTracker(this@IplRewardsController::getEventTracker)
                    }
                }

                is IplRewardsControllerModel.ClaimedTshirtReward -> {
                    claimedTshirtRewardView {
                        id("claimedTshirtRewardView$modelCountBuiltSoFar")
                        reward(it.reward)
                        source(this@IplRewardsController::getSource)
                        eventTracker(this@IplRewardsController::getEventTracker)
                    }
                }

                is IplRewardsControllerModel.UnclaimedMysteryPrize -> {
                    unclaimedMysteryPrizeView {
                        id("unclaimedMysteryPrizeView$modelCountBuiltSoFar")
                        prize(it.prize)
                        source(this@IplRewardsController::getSource)
                        eventTracker(this@IplRewardsController::getEventTracker)
                    }
                }

                is IplRewardsControllerModel.ClaimedMysteryPrize -> {
                    claimedMysteryPrizeView {
                        id("claimedMysteryPrizeView$modelCountBuiltSoFar")
                        prize(it.prize)
                        source(this@IplRewardsController::getSource)
                        eventTracker(this@IplRewardsController::getEventTracker)
                    }
                }
            }
        }
    }

    @JvmName("getEventTracker1")
    private fun getEventTracker() = eventTracker

    @JvmName("getSource1")
    private fun getSource() = source
}
