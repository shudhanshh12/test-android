package `in`.okcredit.merchant.customer_ui.ui.subscription.detail

import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.ui.subscription.detail.SubscriptionDetailContract.Companion.ARG_CUSTOMER_ID
import `in`.okcredit.merchant.customer_ui.ui.subscription.detail.SubscriptionDetailContract.Companion.ARG_SUBSCRIPTION_ID
import `in`.okcredit.merchant.customer_ui.ui.subscription.detail.SubscriptionDetailContract.Companion.ARG_SUBSCRIPTION_OBJECT
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class SubscriptionDetailFragmentModule {
    companion object {

        @Provides
        fun initialState(): SubscriptionDetailContract.State = SubscriptionDetailContract.State()

        @Provides
        @ViewModelParam(ARG_SUBSCRIPTION_ID)
        fun subscriptionId(fragment: SubscriptionDetailFragment): String {
            return fragment.arguments?.getString(ARG_SUBSCRIPTION_ID)
                ?: throw IllegalArgumentException("Subscription id not provided")
        }

        @Provides
        @ViewModelParam(ARG_SUBSCRIPTION_OBJECT)
        fun subscriptionObject(fragment: SubscriptionDetailFragment): Subscription? {
            return fragment.arguments?.getParcelable(ARG_SUBSCRIPTION_OBJECT)
        }

        @Provides
        @ViewModelParam(ARG_CUSTOMER_ID)
        fun customerId(fragment: SubscriptionDetailFragment): String {
            return fragment.arguments?.getString(ARG_CUSTOMER_ID) ?: ""
        }

        @Provides
        fun viewModel(
            fragment: SubscriptionDetailFragment,
            viewModelProvider: Provider<SubscriptionDetailViewModel>
        ): MviViewModel<SubscriptionDetailContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
