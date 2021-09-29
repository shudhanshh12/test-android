package `in`.okcredit.merchant.rewards.ui.rewards_dialog.di_

import `in`.okcredit.analytics.Event
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardActivity
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardsContract
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardsDialog
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardsViewModel
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class ClaimRewardsFragmentModule {

    companion object {

        @Provides
        fun initialState(activity: AppCompatActivity): ClaimRewardsContract.State {
            val reward = activity.intent.getParcelableExtra<RewardModel>(ClaimRewardActivity.EXTRA_REWARD)
            val source = activity.intent.getStringExtra(ClaimRewardActivity.EXTRA_SOURCE)
            var state = ClaimRewardsContract.State.fromReward(reward)

            if (source == Event.PAYMENT_RESULT_SCREEN) {
                state = state.copy(canShowGoToRewardsButton = true)
            }

            return state
        }

        @Provides
        @ViewModelParam(ClaimRewardActivity.EXTRA_REWARD)
        fun rewardModel(activity: AppCompatActivity) =
            activity.intent.getParcelableExtra<RewardModel>(ClaimRewardActivity.EXTRA_REWARD)

        @Provides
        @ViewModelParam(ClaimRewardActivity.EXTRA_SOURCE)
        fun source(activity: AppCompatActivity) = activity.intent.getStringExtra(ClaimRewardActivity.EXTRA_SOURCE)

        @Provides
        @ViewModelParam(ClaimRewardActivity.EXTRA_REFERENCE_ID)
        fun referenceId(activity: AppCompatActivity) =
            activity.intent.getStringExtra(ClaimRewardActivity.EXTRA_REFERENCE_ID)

        @Provides
        fun viewModel(
            fragment: ClaimRewardsDialog,
            viewModelProvider: Provider<ClaimRewardsViewModel>,
        ): MviViewModel<ClaimRewardsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
