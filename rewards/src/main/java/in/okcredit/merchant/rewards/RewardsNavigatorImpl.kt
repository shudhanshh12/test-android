package `in`.okcredit.merchant.rewards

import `in`.okcredit.merchant.rewards.ui.RewardsActivity
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardActivity
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardsNavigator
import android.app.Activity
import android.content.Context
import javax.inject.Inject

class RewardsNavigatorImpl @Inject constructor() :
    RewardsNavigator {

    override fun goToRewardsScreen(context: Context) {
        RewardsActivity.start(context)
    }

    override fun goToClaimRewardScreen(activity: Activity, reward: RewardModel, source: String, referenceId: String) {
        ClaimRewardActivity.start(activity, reward, source, referenceId)
    }
}
