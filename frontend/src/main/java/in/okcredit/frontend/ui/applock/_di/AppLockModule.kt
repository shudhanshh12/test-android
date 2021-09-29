package `in`.okcredit.frontend.ui.applock._di

import `in`.okcredit.frontend.ui.MainActivityTranslucentFullScreen
import `in`.okcredit.frontend.ui.applock.AppLockContract
import `in`.okcredit.frontend.ui.applock.AppLockFragment
import `in`.okcredit.frontend.ui.applock.AppLockViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AppLockModule {

    companion object {

        @Provides
        fun initialState(): AppLockContract.State = AppLockContract.State()

        @Provides
        @ViewModelParam(MainActivityTranslucentFullScreen.ARG_SOURCE)
        fun appLockSource(activity: MainActivityTranslucentFullScreen): String {
            return activity.intent.getStringExtra(MainActivityTranslucentFullScreen.ARG_SOURCE)
        }

        @Provides
        fun viewModel(
            fragment: AppLockFragment,
            viewModelProvider: Provider<AppLockViewModel>
        ): MviViewModel<AppLockContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
