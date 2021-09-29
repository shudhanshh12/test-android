package tech.okcredit.home.ui._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.home.ui.bulk_reminder.BulkReminderBanner.BulkReminderBannerListener
import tech.okcredit.home.ui.customer_tab.CustomerTabContract
import tech.okcredit.home.ui.customer_tab.CustomerTabFragment
import tech.okcredit.home.ui.customer_tab.CustomerTabViewModel
import javax.inject.Provider

@Module
abstract class CustomerTabFragmentModule {

    @Binds
    @FragmentScope
    abstract fun listeners(fragment: CustomerTabFragment): CustomerTabContract.Listeners

    @Reusable
    @Binds
    abstract fun bulkReminderListener(fragment: CustomerTabFragment): BulkReminderBannerListener

    companion object {

        @Provides
        fun initialState(): CustomerTabContract.State = CustomerTabContract.State()

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system
        // (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: CustomerTabFragment,
            viewModelProvider: Provider<CustomerTabViewModel>
        ): MviViewModel<CustomerTabContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
