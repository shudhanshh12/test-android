package tech.okcredit.android.referral.ui.referral_screen.di_

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.android.referral.ui.referral_screen.ReferralContract
import tech.okcredit.android.referral.ui.referral_screen.ReferralFragment
import tech.okcredit.android.referral.ui.referral_screen.ReferralViewModel
import javax.inject.Provider

@Module
abstract class ReferralFragmentModule {

    companion object {

        @Provides
        fun initialState(): ReferralContract.State = ReferralContract.State()

        @Provides
        fun viewModel(
            fragment: ReferralFragment,
            viewModelProvider: Provider<ReferralViewModel>
        ): MviViewModel<ReferralContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
