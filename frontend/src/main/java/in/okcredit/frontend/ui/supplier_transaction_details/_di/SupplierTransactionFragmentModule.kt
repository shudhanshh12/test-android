package `in`.okcredit.frontend.ui.supplier_transaction_details._di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.supplier_transaction_details.SupplierTransactionContract
import `in`.okcredit.frontend.ui.supplier_transaction_details.SupplierTransactionFragment
import `in`.okcredit.frontend.ui.supplier_transaction_details.SupplierTransactionViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class SupplierTransactionFragmentModule {

    companion object {

        @Provides
        @ViewModelParam("transaction_id")
        fun customerId(activity: MainActivity): String? {
            return activity.intent.getStringExtra(MainActivity.ARG_TRANSACTION_ID)
        }

        @Provides
        fun viewModel(
            fragment: SupplierTransactionFragment,
            viewModelProvider: Provider<SupplierTransactionViewModel>
        ): MviViewModel<SupplierTransactionContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
