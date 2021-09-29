package tech.okcredit.android.referral.di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.android.referral.ui.share.ShareReferralContract
import tech.okcredit.android.referral.ui.share.ShareReferralFragment
import tech.okcredit.android.referral.ui.share.ShareReferralViewModel
import javax.inject.Provider

@Module
abstract class ShareReferralFragmentModule {

    companion object {

        @Provides
        fun initialState(): ShareReferralContract.State = ShareReferralContract.State()

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system
        // (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: ShareReferralFragment,
            viewModelProvider: Provider<ShareReferralViewModel>
        ): MviViewModel<ShareReferralContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
