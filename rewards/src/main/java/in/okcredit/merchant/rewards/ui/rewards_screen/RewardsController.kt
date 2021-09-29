package `in`.okcredit.merchant.rewards.ui.rewards_screen

import `in`.okcredit.merchant.rewards.ui.rewards_screen.views.claimedActivationReward
import `in`.okcredit.merchant.rewards.ui.rewards_screen.views.claimedReferralReward
import `in`.okcredit.merchant.rewards.ui.rewards_screen.views.claimedRewardView
import `in`.okcredit.merchant.rewards.ui.rewards_screen.views.unclaimedActivationReward
import `in`.okcredit.merchant.rewards.ui.rewards_screen.views.unclaimedReferralReward
import `in`.okcredit.merchant.rewards.ui.rewards_screen.views.unclaimedRewardView
import com.airbnb.epoxy.AsyncEpoxyController
import javax.inject.Inject

class RewardsController @Inject constructor() : AsyncEpoxyController() {
    private lateinit var state: RewardsContract.State

    fun setState(state: RewardsContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {

        val sortedData = state.rewards.sortedByDescending { it.createdAt }

        sortedData.forEach { controllerModel ->
            when (controllerModel) {
                is RewardsControllerModel.ClaimedReward -> {
                    claimedRewardView {
                        id("claimedRewardView$modelCountBuiltSoFar")
                        reward(controllerModel.reward)
                    }
                }
                is RewardsControllerModel.UnclaimedReward -> {
                    unclaimedRewardView {
                        id("claimedRewardView$modelCountBuiltSoFar")
                        reward(controllerModel.reward)
                    }
                }
                is RewardsControllerModel.ClaimedReferralRewards -> {
                    claimedReferralReward {
                        id("claimedRewardView$modelCountBuiltSoFar")
                        reward(controllerModel.reward)
                    }
                }
                is RewardsControllerModel.UnclaimedReferralRewards -> {
                    unclaimedReferralReward {
                        id("claimedRewardView$modelCountBuiltSoFar")
                        reward(controllerModel.reward)
                    }
                }
                is RewardsControllerModel.ClaimedActivationRewards -> {
                    claimedActivationReward {
                        id("claimedActivationReward$modelCountBuiltSoFar")
                        reward(controllerModel.reward)
                    }
                }
                is RewardsControllerModel.UnclaimedActivationRewards -> {
                    unclaimedActivationReward {
                        id("claimedActivationReward$modelCountBuiltSoFar")
                        reward(controllerModel.reward)
                    }
                }
            }
        }
    }
}
