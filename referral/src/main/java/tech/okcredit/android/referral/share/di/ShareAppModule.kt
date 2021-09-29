package tech.okcredit.android.referral.share.di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.android.referral.share.ShareAppContract
import tech.okcredit.android.referral.share.ShareAppFragment
import tech.okcredit.android.referral.share.ShareAppViewModel
import javax.inject.Provider

@Module
abstract class ShareAppModule {

    companion object {

        @Provides
        fun viewModel(
            fragment: ShareAppFragment,
            viewModelProvider: Provider<ShareAppViewModel>
        ): MviViewModel<ShareAppContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
