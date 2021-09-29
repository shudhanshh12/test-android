package `in`.okcredit.rewards.contract

import android.app.Activity
import android.content.Context

interface RewardsNavigator {
    fun goToRewardsScreen(context: Context)

    fun goToClaimRewardScreen(activity: Activity, reward: RewardModel, source: String, referenceId: String)
}
