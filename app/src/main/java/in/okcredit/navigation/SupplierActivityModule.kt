package `in`.okcredit.navigation

import `in`.okcredit.frontend.ui.add_supplier_transaction.AddSupplierTransactionFragment
import `in`.okcredit.frontend.ui.add_supplier_transaction._di.AddSupplierTransactionFragmentModule
import `in`.okcredit.frontend.ui.supplier.SupplierFragment
import `in`.okcredit.frontend.ui.supplier._di.SupplierFragmentModule
import `in`.okcredit.frontend.ui.supplier_reports.SupplierReportsFragment
import `in`.okcredit.frontend.ui.supplier_reports.di.SupplierReportsModule
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogScreen
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog._di.AddNumberDialogModule
import `in`.okcredit.payment.ui.add_payment_dialog.AddPaymentDestinationDialog
import `in`.okcredit.payment.ui.add_payment_dialog._di.AddPaymentDestinationModule
import `in`.okcredit.payment.ui.blindpay.BlindPayDialog
import `in`.okcredit.payment.ui.blindpay.di.BlindPayModule
import `in`.okcredit.shared.calculator.CalculatorLayout
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderScreen
import `in`.okcredit.shared.dialogs.bottomsheetloader._di.BottomSheetLoaderModule
import `in`.okcredit.shared.mini_calculator.MiniCalculatorLayout
import dagger.Module
import dagger.android.ContributesAndroidInjector
import merchant.okcredit.accounting.ui.transaction_sort_options_dialog.TransactionsSortCriteriaSelectionBottomSheet
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class SupplierActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [SupplierFragmentModule::class])
    abstract fun supplierFragment(): SupplierFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [AddPaymentDestinationModule::class])
    abstract fun addPaymentDialog(): AddPaymentDestinationDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [SupplierReportsModule::class])
    abstract fun supplierReport(): SupplierReportsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [AddSupplierTransactionFragmentModule::class])
    abstract fun addSupplierTransactionFragment(): AddSupplierTransactionFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [BottomSheetLoaderModule::class])
    abstract fun bottomSheetLoader(): BottomSheetLoaderScreen

    @FragmentScope
    @ContributesAndroidInjector(modules = [AddNumberDialogModule::class])
    abstract fun addMobileNumberDialog(): AddNumberDialogScreen

    @ContributesAndroidInjector
    abstract fun calculatorLayout(): CalculatorLayout

    @ContributesAndroidInjector
    abstract fun miniCalculatorLayout(): MiniCalculatorLayout

    @ContributesAndroidInjector(modules = [BlindPayModule::class])
    abstract fun blindPayDialog(): BlindPayDialog

    @ContributesAndroidInjector
    abstract fun transactionsSortCriteriaSelectionBottomSheet(): TransactionsSortCriteriaSelectionBottomSheet
}
