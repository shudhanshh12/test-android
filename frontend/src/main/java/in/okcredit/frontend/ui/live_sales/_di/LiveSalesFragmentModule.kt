package `in`.okcredit.frontend.ui.live_sales._di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.live_sales.LiveSalesContract
import `in`.okcredit.frontend.ui.live_sales.LiveSalesFragment
import `in`.okcredit.frontend.ui.live_sales.LiveSalesViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class LiveSalesFragmentModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: LiveSalesFragment): LiveSalesContract.Navigator

    companion object {

        @Provides
        fun initialState(): LiveSalesContract.State = LiveSalesContract.State()

        @Provides
        @ViewModelParam(MainActivity.ARG_CUSTOMER_ID)
        fun customerId(activity: MainActivity): String {
            return activity.intent.getStringExtra(MainActivity.ARG_CUSTOMER_ID)
        }

        @Provides
        fun viewModel(
            fragment: LiveSalesFragment,
            viewModelProvider: Provider<LiveSalesViewModel>
        ): MviViewModel<LiveSalesContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
