package `in`.okcredit.frontend.ui.supplier._di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.SupplierActivity
import `in`.okcredit.frontend.ui.supplier.SupplierContract
import `in`.okcredit.frontend.ui.supplier.SupplierFragment
import `in`.okcredit.frontend.ui.supplier.SupplierFragment.Companion.ARG_SCREEN_REDIRECT_TO_PAYMENT
import `in`.okcredit.frontend.ui.supplier.SupplierScreenViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class SupplierFragmentModule {

    companion object {

        @Provides
        fun initialState(activity: SupplierActivity): SupplierContract.State {
            return SupplierContract.State(
                redirectToPayment = activity.intent.getBooleanExtra(
                    ARG_SCREEN_REDIRECT_TO_PAYMENT,
                    false
                )
            )
        }

        @Provides
        @ViewModelParam(MainActivity.ARG_SUPPLIER_ID)
        fun supplierId(activity: SupplierActivity): String {
            return activity.intent.getStringExtra(MainActivity.ARG_SUPPLIER_ID)
        }

        @Provides
        @ViewModelParam(MainActivity.ARG_TXN_ID)
        fun txnId(activity: SupplierActivity): String {
            return activity.intent.getStringExtra(MainActivity.ARG_TXN_ID) ?: ""
        }

        @Provides
        @ViewModelParam(MainActivity.REACTIVATE)
        fun reactivate(activity: SupplierActivity): Boolean {
            return activity.intent.getBooleanExtra(MainActivity.REACTIVATE, false)
        }

        @Provides
        @ViewModelParam(MainActivity.NAME)
        fun supplierName(activity: SupplierActivity): String {
            val name = activity.intent.getStringExtra(MainActivity.NAME)
            return name ?: ""
        }

        @Provides
        fun viewModel(
            fragment: SupplierFragment,
            viewModelProvider: Provider<SupplierScreenViewModel>,
        ): MviViewModel<SupplierContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
