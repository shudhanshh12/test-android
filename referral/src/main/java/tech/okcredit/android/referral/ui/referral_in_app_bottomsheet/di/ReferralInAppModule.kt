package tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.ReferralInAppBottomSheet
import tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.ReferralInAppContract
import tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.ReferralInAppViewModel
import javax.inject.Provider

@Module
abstract class ReferralInAppModule {

    companion object {

        @Provides
        fun initialState(): ReferralInAppContract.State = ReferralInAppContract.State

        @Provides
        fun viewModel(
            fragment: ReferralInAppBottomSheet,
            viewModelProvider: Provider<ReferralInAppViewModel>
        ): MviViewModel<ReferralInAppContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
