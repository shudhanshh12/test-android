package `in`.okcredit.merchant.customer_ui.ui.customer._di

import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerContract
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerFragment
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerFragment.Companion.ARG_SCREEN_REDIRECT_TO_PAYMENT
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerScreenViewModel
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class CustomerFragmentModule {

    companion object {

        @Provides
        fun initialState(activity: AppCompatActivity): CustomerContract.State {
            return CustomerContract.State(
                redirectToPayment = activity.intent.getBooleanExtra(
                    ARG_SCREEN_REDIRECT_TO_PAYMENT,
                    false
                )
            )
        }

        @Provides
        @ViewModelParam(CustomerContract.ARG_CUSTOMER_ID)
        fun customerId(activity: AppCompatActivity): String {
            return activity.intent.getStringExtra(CustomerContract.ARG_CUSTOMER_ID) ?: ""
        }

        @Provides
        @ViewModelParam(CustomerContract.REACTIVATE)
        fun reactivate(activity: AppCompatActivity): Boolean {
            return activity.intent.getBooleanExtra(CustomerContract.REACTIVATE, false)
        }

        @Provides
        @ViewModelParam(CustomerContract.ARG_TXN_ID)
        fun txnId(activity: AppCompatActivity): String {
            return activity.intent.getStringExtra(CustomerContract.ARG_TXN_ID) ?: ""
        }

        @Provides
        @ViewModelParam(CustomerContract.NAME)
        fun customerName(activity: AppCompatActivity): String {
            return activity.intent.getStringExtra(CustomerContract.NAME) ?: ""
        }

        @Provides
        @ViewModelParam(CustomerContract.ARG_SOURCE)
        fun sourceScreen(activity: AppCompatActivity): String? {
            return activity.intent.getStringExtra(CustomerContract.ARG_SOURCE)
        }

        @Provides
        @ViewModelParam(CustomerContract.ARG_COLLECTION_ID)
        fun collectionId(activity: AppCompatActivity): String? {
            return activity.intent.getStringExtra(CustomerContract.ARG_COLLECTION_ID)
        }

        @Provides
        fun viewModel(
            fragment: CustomerFragment,
            viewModelProvider: Provider<CustomerScreenViewModel>,
        ): MviViewModel<CustomerContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
