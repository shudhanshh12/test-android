package `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay.di

import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerContract
import `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay.CollectWithGooglePayBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay.CollectWithGooglePayContract
import `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay.CollectWithGooglePayViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class CollectWithGooglePayModule {

    companion object {

        @Provides
        fun initialState(fragment: CollectWithGooglePayBottomSheet): CollectWithGooglePayContract.State {
            val customerId = fragment.requireArguments().getString(CollectWithGooglePayContract.ARG_CUSTOMER_ID)
                ?: throw IllegalArgumentException("Missing Customer ID")
            return CollectWithGooglePayContract.State(accountId = customerId)
        }

        @Provides
        @ViewModelParam(CustomerContract.ARG_CUSTOMER_ID)
        fun customerId(fragment: CollectWithGooglePayBottomSheet): String {
            return fragment.requireArguments().getString(CollectWithGooglePayContract.ARG_CUSTOMER_ID)
                ?: throw IllegalArgumentException("Missing Customer ID")
        }

        @Provides
        fun viewModel(
            fragment: CollectWithGooglePayBottomSheet,
            viewModelProvider: Provider<CollectWithGooglePayViewModel>,
        ): MviViewModel<CollectWithGooglePayContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
