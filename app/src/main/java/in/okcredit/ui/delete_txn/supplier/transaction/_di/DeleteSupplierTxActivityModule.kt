package `in`.okcredit.ui.delete_txn.supplier.transaction._di

import `in`.okcredit.ui.delete_txn.supplier.transaction.DeleteSupplierTransactionActivity
import `in`.okcredit.ui.delete_txn.supplier.transaction.DeleteSupplierTxnContract
import `in`.okcredit.ui.delete_txn.supplier.transaction.DeleteSupplierTxnPresenter
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.ActivityScope

@Module
abstract class DeleteSupplierTxActivityModule {
    @Binds
    @ActivityScope
    abstract fun viewModel(
        viewModel: DeleteSupplierTxnPresenter
    ): DeleteSupplierTxnContract.Presenter?

    companion object {

        @Provides
        @ActivityScope
        @ViewModelParam("tx_id")
        fun transactionId(activity: DeleteSupplierTransactionActivity): String =
            activity.intent.getStringExtra(DeleteSupplierTransactionActivity.EXTRA_TX_ID)
    }
}
