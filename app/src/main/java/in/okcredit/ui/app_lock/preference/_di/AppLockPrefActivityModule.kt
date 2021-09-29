package `in`.okcredit.ui.app_lock.preference._di

import `in`.okcredit.ui.app_lock.preference.AppLockPrefContract
import `in`.okcredit.ui.app_lock.preference.AppLockPrefPresenter
import dagger.Binds
import dagger.Module
import tech.okcredit.base.dagger.di.scope.ActivityScope

@Module
abstract class AppLockPrefActivityModule {

    @Binds
    @ActivityScope
    abstract fun viewModel(viewModel: AppLockPrefPresenter): AppLockPrefContract.Presenter
}
