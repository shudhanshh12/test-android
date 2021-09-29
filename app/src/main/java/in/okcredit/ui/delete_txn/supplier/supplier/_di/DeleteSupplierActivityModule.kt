package `in`.okcredit.ui.delete_txn.supplier.supplier._di

import `in`.okcredit.ui.delete_txn.supplier.supplier.DeleteSupplierActivity
import `in`.okcredit.ui.delete_txn.supplier.supplier.DeleteSupplierContract
import `in`.okcredit.ui.delete_txn.supplier.supplier.DeleteSupplierPresenter
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.ActivityScope

@Module
abstract class DeleteSupplierActivityModule {

    @Binds
    @ActivityScope
    abstract fun viewModel(viewModel: DeleteSupplierPresenter): DeleteSupplierContract.Presenter?

    companion object {

        @Provides
        @ActivityScope
        @ViewModelParam("customer_id")
        fun customerId(activity: DeleteSupplierActivity): String =
            activity.intent.getStringExtra(DeleteSupplierActivity.EXTRA_SUPPLIER_ID)
    }
}
