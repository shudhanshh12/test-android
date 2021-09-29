package `in`.okcredit.frontend.ui.payment_password._di

import `in`.okcredit.frontend.ui.payment_password.PasswordEnableContract
import `in`.okcredit.frontend.ui.payment_password.PasswordEnableFragment
import `in`.okcredit.frontend.ui.payment_password.PasswordEnableViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class PasswordEnableFragmentModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: PasswordEnableFragment): PasswordEnableContract.Navigator

    companion object {

        @Provides
        fun viewModel(
            fragment: PasswordEnableFragment,
            viewModelProvider: Provider<PasswordEnableViewModel>
        ): MviViewModel<PasswordEnableContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
