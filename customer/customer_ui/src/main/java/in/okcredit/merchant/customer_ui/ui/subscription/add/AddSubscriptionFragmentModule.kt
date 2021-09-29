package `in`.okcredit.merchant.customer_ui.ui.subscription.add

import `in`.okcredit.merchant.customer_ui.ui.subscription.SubscriptionActivity
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddSubscriptionFragmentModule {
    companion object {

        @Provides
        fun initialState(): AddSubscriptionContract.State = AddSubscriptionContract.State()

        @Provides
        @ViewModelParam("customer_id")
        fun customerId(fragment: AddSubscriptionFragment, activity: SubscriptionActivity): String? {
            return fragment.arguments?.getString("customer_id")
                ?: activity.intent.getStringExtra("customer_id")
        }

        @Provides
        fun viewModel(
            fragment: AddSubscriptionFragment,
            viewModelProvider: Provider<AddSubscriptionViewModel>
        ): MviViewModel<AddSubscriptionContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
