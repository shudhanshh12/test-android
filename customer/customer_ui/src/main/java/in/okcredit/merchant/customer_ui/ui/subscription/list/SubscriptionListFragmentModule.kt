package `in`.okcredit.merchant.customer_ui.ui.subscription.list

import `in`.okcredit.merchant.customer_ui.ui.subscription.SubscriptionActivity
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class SubscriptionListFragmentModule {

    companion object {

        @Provides
        fun initialState(): SubscriptionListContract.State = SubscriptionListContract.State()

        @Provides
        @ViewModelParam("customer_id")
        fun customerId(fragment: SubscriptionListFragment, activity: SubscriptionActivity): String? {
            return fragment.arguments?.getString("customer_id")
                ?: activity.intent.getStringExtra("customer_id")
        }

        @Provides
        fun presenter(
            fragment: SubscriptionListFragment,
            viewModelProvider: Provider<SubscriptionListViewModel>
        ): MviViewModel<SubscriptionListContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
