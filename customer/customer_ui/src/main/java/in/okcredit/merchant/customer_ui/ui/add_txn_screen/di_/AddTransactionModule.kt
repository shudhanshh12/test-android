package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.di_

import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionContract
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionFragment
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionViewModel
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Companion.ADD_TRANSACTION_ROBOFLOW
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Companion.AMOUNT
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Companion.CUSTOMER_ID
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Companion.SOURCE
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Companion.TRANSACTION_TYPE
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Source
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Source.*
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.AddBillsView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.PictureView
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views.RoboflowPictureView.*
import `in`.okcredit.shared.base.MviViewModel
import android.app.Activity
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddTransactionModule {

    @Binds
    abstract fun addPictureViewListener(fragment: AddTransactionFragment): PictureView.Listener

    @Binds
    abstract fun addRoboflowPictureListener(fragment: AddTransactionFragment): Listener

    @Binds
    abstract fun addBillsViewListener(fragment: AddTransactionFragment): AddBillsView.Listener

    companion object {

        @Provides
        fun initialState(
            fragment: AddTransactionFragment,
            activity: Activity
        ): AddTransactionContract.State =
            AddTransactionContract.State().copy(
                source = activity.intent.getSerializableExtra(SOURCE) as? Source ?: CUSTOMER_SCREEN,
                amount = activity.intent.getLongExtra("amount", 0),
                isRoboflowEnabled = fragment.arguments?.getBoolean(ADD_TRANSACTION_ROBOFLOW) ?: false
            )

        @Provides
        @ViewModelParam(CUSTOMER_ID)
        fun customerId(fragment: AddTransactionFragment, activity: Activity): String? {
            return fragment.arguments?.getString(CUSTOMER_ID)
                ?: activity.intent.getStringExtra(CUSTOMER_ID)
        }

        @Provides
        @ViewModelParam(AMOUNT)
        fun amount(activity: Activity): Long {
            return activity.intent.getLongExtra(AMOUNT, 0)
        }

        @Provides
        @ViewModelParam(TRANSACTION_TYPE)
        fun transactionType(fragment: AddTransactionFragment, activity: Activity): Int {
            return fragment.arguments?.getInt(TRANSACTION_TYPE)?.takeUnless { it == 0 }
                ?: activity.intent.getIntExtra(
                    TRANSACTION_TYPE,
                    merchant.okcredit.accounting.model.Transaction.CREDIT
                )
        }

        @Provides
        @ViewModelParam(ADD_TRANSACTION_ROBOFLOW)
        fun roboflowExperiment(fragment: AddTransactionFragment): Boolean {
            return fragment.arguments?.getBoolean(ADD_TRANSACTION_ROBOFLOW) ?: false
        }

        @Provides
        fun presenter(
            fragment: AddTransactionFragment,
            viewModelProvider: Provider<AddTransactionViewModel>
        ): MviViewModel<AddTransactionContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
