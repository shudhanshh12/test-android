package merchant.okcredit.gamification.ipl.rewards._di

import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardActivity
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardContract
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardFragment
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardPresenter
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class ClaimRewardFragmentModule {

    companion object {

        @Provides
        @ViewModelParam(ClaimRewardActivity.EXTRA_REWARD)
        fun reward(activity: ClaimRewardActivity): RewardModel {
            return activity.intent.getParcelableExtra(ClaimRewardActivity.EXTRA_REWARD)
        }

        @Provides
        @ViewModelParam(ClaimRewardActivity.EXTRA_SOURCE)
        fun source(activity: ClaimRewardActivity): String {
            return activity.intent.getStringExtra(ClaimRewardActivity.EXTRA_SOURCE) ?: throw IllegalArgumentException("Must send source for claim rewards")
        }

        @Provides
        fun initialState(activity: AppCompatActivity): ClaimRewardContract.State {
            val reward = activity.intent.getParcelableExtra<RewardModel>(ClaimRewardActivity.EXTRA_REWARD)
            return ClaimRewardContract.State.fromReward(reward)
        }

        @Provides
        fun viewModel(
            fragment: ClaimRewardFragment,
            viewModelProvider: Provider<ClaimRewardPresenter>
        ): MviViewModel<ClaimRewardContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
