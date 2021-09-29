package `in`.okcredit.frontend.ui.number_change._di

import `in`.okcredit.frontend.ui.number_change.InfoChangeNumberContract
import `in`.okcredit.frontend.ui.number_change.InfoChangeNumberFragment
import `in`.okcredit.frontend.ui.number_change.InfoChangeNumberViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class InfoChangeNumberModule {

    @Binds
    @FragmentScope
    abstract fun navigator(numberFragment: InfoChangeNumberFragment): InfoChangeNumberContract.Navigator

    companion object {

        @Provides
        fun initialState(): InfoChangeNumberContract.State = InfoChangeNumberContract.State()

        @Provides
        fun viewModel(
            numberFragment: InfoChangeNumberFragment,
            numberViewModelProvider: Provider<InfoChangeNumberViewModel>
        ): MviViewModel<InfoChangeNumberContract.State> = numberFragment.createViewModel(numberViewModelProvider)
    }
}
