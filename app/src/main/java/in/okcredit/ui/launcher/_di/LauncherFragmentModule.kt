package `in`.okcredit.ui.launcher._di

import `in`.okcredit.onboarding.launcher.LauncherContract
import `in`.okcredit.onboarding.launcher.LauncherPresenter
import dagger.Binds
import dagger.Module

@Module
abstract class LauncherFragmentModule {

    @Binds
    abstract fun viewModel(viewModel: LauncherPresenter): LauncherContract.Presenter
}
