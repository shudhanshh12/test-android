package `in`.okcredit.ui.reset_pwd._di

import `in`.okcredit.ui.reset_pwd.password.PasswordContract
import `in`.okcredit.ui.reset_pwd.password.PasswordFragment
import `in`.okcredit.ui.reset_pwd.password.PasswordPresenter
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam

@Module
abstract class PasswordFragmentModule {

    @Binds
    abstract fun viewModel(viewModel: PasswordPresenter): PasswordContract.Presenter

    companion object {

        @Provides
        @ViewModelParam("requested_screen")
        fun requestedScreen(fragment: PasswordFragment): String {
            return fragment.requireArguments().getString(PasswordFragment.ARG_REQUESTED_SCREEN)!!
        }
    }
}
