package `in`.okcredit.frontend.ui.add_supplier_transaction._di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.SupplierActivity
import `in`.okcredit.frontend.ui.add_supplier_transaction.AddSupplierTransactionContract
import `in`.okcredit.frontend.ui.add_supplier_transaction.AddSupplierTransactionFragment
import `in`.okcredit.frontend.ui.add_supplier_transaction.AddSupplierTxnScreenViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class AddSupplierTransactionFragmentModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: AddSupplierTransactionFragment): AddSupplierTransactionContract.Navigator

    companion object {

        @Provides
        fun initialState(): AddSupplierTransactionContract.State = AddSupplierTransactionContract.State()

        @Provides
        @ViewModelParam(MainActivity.ARG_SUPPLIER_ID)
        fun supplierId(fragment: AddSupplierTransactionFragment, activity: SupplierActivity): String? {
            return fragment.arguments?.getString(MainActivity.ARG_SUPPLIER_ID)
                ?: activity.intent.getStringExtra(MainActivity.ARG_SUPPLIER_ID)
        }

        @Provides
        @ViewModelParam("transaction_type")
        fun transactionType(fragment: AddSupplierTransactionFragment, activity: SupplierActivity): Int? {
            return fragment.arguments?.getInt("transaction_type")
                ?.takeUnless { it == 0 }
                ?: activity.intent.getIntExtra(
                    "transaction_type",
                    merchant.okcredit.accounting.model.Transaction.CREDIT
                )
        }

        @Provides
        @ViewModelParam("transaction_amount")
        fun transactionAmount(activity: SupplierActivity): Long {
            return activity.intent.getLongExtra(MainActivity.ARG_TX_AMOUNT, 0)
        }

        @Provides
        fun viewModel(
            fragment: AddSupplierTransactionFragment,
            viewModelProvider: Provider<AddSupplierTxnScreenViewModel>
        ): MviViewModel<AddSupplierTransactionContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
