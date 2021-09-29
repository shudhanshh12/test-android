package `in`.okcredit.frontend.ui.privacy._di

import `in`.okcredit.frontend.ui.privacy.PrivacyContract
import `in`.okcredit.frontend.ui.privacy.PrivacyFragment
import `in`.okcredit.frontend.ui.privacy.PrivacyViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class PrivacyFragmentModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: PrivacyFragment): PrivacyContract.Navigator

    companion object {

        @Provides
        fun viewModel(
            fragment: PrivacyFragment,
            viewModelProvider: Provider<PrivacyViewModel>
        ): MviViewModel<PrivacyContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
