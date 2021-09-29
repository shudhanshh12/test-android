package `in`.okcredit.onboarding.change_number

import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class ChangeNumberModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: ChangeNumberFragment): ChangeNumberContract.Navigator

    companion object {

        @Provides
        fun initialState(): ChangeNumberContract.State = ChangeNumberContract.State()

        @Provides
        fun viewModel(
            fragment: ChangeNumberFragment,
            viewModelProvider: Provider<ChangeNumberViewModel>
        ): MviViewModel<ChangeNumberContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
