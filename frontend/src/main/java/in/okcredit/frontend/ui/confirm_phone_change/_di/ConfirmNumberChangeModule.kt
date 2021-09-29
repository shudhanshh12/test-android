package `in`.okcredit.frontend.ui.confirm_phone_change._di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.confirm_phone_change.ConfirmNumberChangeContract
import `in`.okcredit.frontend.ui.confirm_phone_change.ConfirmNumberChangeFragment
import `in`.okcredit.frontend.ui.confirm_phone_change.ConfirmNumberChangeViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class ConfirmNumberChangeModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: ConfirmNumberChangeFragment): ConfirmNumberChangeContract.Navigator

    companion object {

        @Provides
        fun initialState(): ConfirmNumberChangeContract.State = ConfirmNumberChangeContract.State()

        @Provides
        @ViewModelParam("temp_new_number")
        fun provideTempNewNumber(activity: MainActivity): String {
            return activity.intent.getStringExtra(MainActivity.TEMP_NEW_NUMBER)
        }

        @Provides
        fun viewModel(
            fragment: ConfirmNumberChangeFragment,
            viewModelProvider: Provider<ConfirmNumberChangeViewModel>
        ): MviViewModel<ConfirmNumberChangeContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
