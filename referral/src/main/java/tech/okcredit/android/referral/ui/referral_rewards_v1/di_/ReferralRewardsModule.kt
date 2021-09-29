package tech.okcredit.android.referral.ui.referral_rewards_v1.di_

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.android.referral.rewards.ui.ReferralRewardsContract
import tech.okcredit.android.referral.ui.ReferralActivity
import tech.okcredit.android.referral.ui.referral_rewards_v1.ReferralRewardsFragment
import tech.okcredit.android.referral.ui.referral_rewards_v1.ReferralRewardsViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class ReferralRewardsModule {

    companion object {

        @Provides
        fun initialState() = ReferralRewardsContract.State()

        @Provides
        @ViewModelParam(ReferralActivity.ARG_TARGETED_REFERRAL_PHONE_NUMBER)
        fun scrollToPhoneNumberOnce(fragment: ReferralRewardsFragment): String? {
            return fragment.arguments?.getString(ReferralActivity.ARG_TARGETED_REFERRAL_PHONE_NUMBER)
                .also { println("http >>>> $it") }
        }

        @Provides
        fun viewModel(
            fragment: ReferralRewardsFragment,
            viewModelProvider: Provider<ReferralRewardsViewModel>,
        ): MviViewModel<ReferralRewardsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
