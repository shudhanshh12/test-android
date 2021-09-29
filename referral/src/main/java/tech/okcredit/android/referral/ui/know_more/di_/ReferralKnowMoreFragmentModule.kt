package tech.okcredit.android.referral.ui.know_more.di_

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.android.referral.ui.know_more.ReferralKnowMoreContract
import tech.okcredit.android.referral.ui.know_more.ReferralKnowMoreFragment
import tech.okcredit.android.referral.ui.know_more.ReferralKnowMoreViewModel
import javax.inject.Provider

@Module
abstract class ReferralKnowMoreFragmentModule {

    companion object {

        @Provides
        fun initialState(): ReferralKnowMoreContract.State = ReferralKnowMoreContract.State()

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system
        // (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: ReferralKnowMoreFragment,
            viewModelProvider: Provider<ReferralKnowMoreViewModel>
        ): MviViewModel<ReferralKnowMoreContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
