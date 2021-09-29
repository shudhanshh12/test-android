package `in`.okcredit.ui.app_lock.set._di

import `in`.okcredit.ui.app_lock.set.AppLockContract
import `in`.okcredit.ui.app_lock.set.AppLockPresenter
import dagger.Binds
import dagger.Module
import tech.okcredit.base.dagger.di.scope.ActivityScope

@Module
abstract class AppLockActivityModule {

    @Binds
    @ActivityScope
    abstract fun viewModel(viewModel: AppLockPresenter): AppLockContract.Presenter
}
