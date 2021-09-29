package `in`.okcredit.ui.delete_txn._di

import `in`.okcredit.ui.delete_txn.DeleteTransactionActivity
import `in`.okcredit.ui.delete_txn.DeleteTxnContract
import `in`.okcredit.ui.delete_txn.DeleteTxnPresenter
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.ActivityScope

@Module
abstract class DeleteTxActivityModule {
    @Binds
    @ActivityScope
    abstract fun viewModel(viewModel: DeleteTxnPresenter): DeleteTxnContract.Presenter

    companion object {

        @Provides
        @ActivityScope
        @ViewModelParam("tx_id")
        fun transactionId(activity: DeleteTransactionActivity): String =
            activity.intent.getStringExtra(DeleteTransactionActivity.EXTRA_TX_ID)
    }
}
