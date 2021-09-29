package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.di_

import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionFragment
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerContract
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerViewModel
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
import javax.inject.Provider

@Module
abstract class AddTxnActivityModule {

    @Binds
    abstract fun addTxnTransparentActivity(addTxnTransparentActivity: AddTxnContainerActivity): Activity

    @ContributesAndroidInjector(modules = [AddTransactionModule::class])
    abstract fun addTxnScreen(): AddTransactionFragment

    @ContributesAndroidInjector(modules = [BottomSheetLoaderModule::class])
    abstract fun bottomSheetLoader(): BottomSheetLoaderScreen

    @ContributesAndroidInjector
    abstract fun calculatorLayout(): CalculatorLayout

    companion object {
        @Provides
        fun initialState(activity: Activity): AddTxnContainerContract.State {
            val customerId = activity.intent.getStringExtra(AddTxnContainerActivity.CUSTOMER_ID)
                ?: throw IllegalArgumentException()
            return AddTxnContainerContract.State(
                customerId = customerId
            )
        }

        @Provides
        fun viewModel(
            fragment: AddTxnContainerActivity,
            viewModelProvider: Provider<AddTxnContainerViewModel>,
        ): MviViewModel<AddTxnContainerContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
