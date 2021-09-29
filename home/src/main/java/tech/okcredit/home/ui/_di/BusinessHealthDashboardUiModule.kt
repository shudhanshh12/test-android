package tech.okcredit.home.ui._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardContract
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardFragment
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardViewModel
import javax.inject.Provider

@Module
abstract class BusinessHealthDashboardUiModule {
    companion object {

        @Provides
        fun initialState(): BusinessHealthDashboardContract.State {
            return BusinessHealthDashboardContract.State()
        }

        @Provides
        fun viewModel(
            fragment: BusinessHealthDashboardFragment,
            viewModelProvider: Provider<BusinessHealthDashboardViewModel>
        ): MviViewModel<BusinessHealthDashboardContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
