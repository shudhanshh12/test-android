package `in`.okcredit.merchant.customer_ui.ui.payment

import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionFragment
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.di_.AddTransactionModule
import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.shared.calculator.CalculatorLayout
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderScreen
import `in`.okcredit.shared.dialogs.bottomsheetloader._di.BottomSheetLoaderModule
import android.app.Activity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddCustomerPaymentModule {

    @Binds
    abstract fun addCustomerPaymentActivity(activity: AddCustomerPaymentActivity): Activity

    @ContributesAndroidInjector(modules = [AddTransactionModule::class])
    abstract fun addTxnScreen(): AddTransactionFragment

    @ContributesAndroidInjector(modules = [BottomSheetLoaderModule::class])
    abstract fun bottomSheetLoader(): BottomSheetLoaderScreen

    @ContributesAndroidInjector
    abstract fun calculatorLayout(): CalculatorLayout

    companion object {

        @Provides
        fun initialState(activity: Activity): AddCustomerPaymentContract.State {
            val customerId = activity.intent.getStringExtra(AddTxnContainerActivity.CUSTOMER_ID)
                ?: throw IllegalArgumentException()
            val source = activity.intent.getStringExtra(AddTxnContainerActivity.SOURCE)
                ?: throw IllegalArgumentException()
            return AddCustomerPaymentContract.State(
                source = source,
                customerId = customerId
            )
        }

        @Provides
        @ViewModelParam("expanded_qr")
        fun customerIds(activity: Activity): Boolean {
            return activity.intent.getBooleanExtra(AddCustomerPaymentActivity.EXTRA_EXPANDED_QR, false)
        }

        @Provides
        fun viewModel(
            fragment: AddCustomerPaymentActivity,
            viewModelProvider: Provider<AddCustomerPaymentViewModel>,
        ): MviViewModel<AddCustomerPaymentContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
