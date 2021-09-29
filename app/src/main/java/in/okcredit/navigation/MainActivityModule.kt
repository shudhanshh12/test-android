package `in`.okcredit.navigation

import `in`.okcredit.collection_ui.ui.home.CollectionsHomeActivityModule
import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.account_statement.AccountStatementFragment
import `in`.okcredit.frontend.ui.account_statement._di.AccountStatementModule
import `in`.okcredit.frontend.ui.add_expense.AddExpenseFragment
import `in`.okcredit.frontend.ui.add_expense._di.AddExpenseModule
import `in`.okcredit.frontend.ui.confirm_phone_change.ConfirmNumberChangeFragment
import `in`.okcredit.frontend.ui.confirm_phone_change._di.ConfirmNumberChangeModule
import `in`.okcredit.frontend.ui.enter_otp._di.EnterOtpFragmentModule
import `in`.okcredit.frontend.ui.expense_manager.ExpenseManagerFragment
import `in`.okcredit.frontend.ui.expense_manager._di.ExpenseManagerModule
import `in`.okcredit.frontend.ui.know_more.KnowMoreFragment
import `in`.okcredit.frontend.ui.know_more._di.KnowMoreModule
import `in`.okcredit.frontend.ui.live_sales.LiveSalesFragment
import `in`.okcredit.frontend.ui.live_sales._di.LiveSalesFragmentModule
import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.CategoryFragment
import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen._di.CategoryFragmentModule
import `in`.okcredit.frontend.ui.merchant_profile.di.MerchantFragmentModule
import `in`.okcredit.frontend.ui.migrate_to_supplier.MoveToSupplierFragment
import `in`.okcredit.frontend.ui.migrate_to_supplier.di.MoveToSupplierModule
import `in`.okcredit.frontend.ui.number_change.InfoChangeNumberFragment
import `in`.okcredit.frontend.ui.number_change._di.InfoChangeNumberModule
import `in`.okcredit.frontend.ui.onboarding.AutoLangModule
import `in`.okcredit.frontend.ui.otp_verification._di.OtpVerificationFragmentModule
import `in`.okcredit.frontend.ui.payment_password.PasswordEnableFragment
import `in`.okcredit.frontend.ui.payment_password._di.PasswordEnableFragmentModule
import `in`.okcredit.frontend.ui.privacy.PrivacyFragment
import `in`.okcredit.frontend.ui.privacy._di.PrivacyFragmentModule
import `in`.okcredit.frontend.ui.supplier_reports.SupplierReportsFragment
import `in`.okcredit.frontend.ui.supplier_reports.di.SupplierReportsModule
import `in`.okcredit.frontend.ui.supplier_transaction_details.SupplierTransactionFragment
import `in`.okcredit.frontend.ui.supplier_transaction_details._di.SupplierTransactionFragmentModule
import `in`.okcredit.frontend.ui.sync.SyncFragment
import `in`.okcredit.frontend.ui.sync._di.SyncModule
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogScreen
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog._di.AddNumberDialogModule
import `in`.okcredit.merchant.customer_ui.ui.add_discount.AddDiscountFragment
import `in`.okcredit.merchant.customer_ui.ui.add_discount._di.AddDiscountFragmentModule
import `in`.okcredit.merchant.customer_ui.ui.buyer_txn_alert.CustomerTxnAlertDialogModule
import `in`.okcredit.merchant.customer_ui.ui.buyer_txn_alert.CustomerTxnAlertDialogScreen
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerFragment
import `in`.okcredit.merchant.customer_ui.ui.customer.VoiceInputBottomSheetFragment
import `in`.okcredit.merchant.customer_ui.ui.customer._di.CustomerFragmentModule
import `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay.CollectWithGooglePayBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay.di.CollectWithGooglePayModule
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet._di.MenuOptionBottomSheetModule
import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsFragment
import `in`.okcredit.merchant.customer_ui.ui.customerreports._di.CustomerReportsModule
import `in`.okcredit.merchant.customer_ui.ui.discount_details.DiscountDetailsFragment
import `in`.okcredit.merchant.customer_ui.ui.discount_details._di.DiscountDetailsFragmentModule
import `in`.okcredit.merchant.customer_ui.ui.discount_info.CustomerAddTxnDiscountInfoDialogModule
import `in`.okcredit.merchant.customer_ui.ui.discount_info.CustomerAddTxnDiscountInfoDialogScreen
import `in`.okcredit.merchant.customer_ui.ui.due_customer.DueCustomerFragment
import `in`.okcredit.merchant.customer_ui.ui.due_customer._di.DueCustomerModule
import `in`.okcredit.merchant.customer_ui.ui.transaction_details.TransactionFragment
import `in`.okcredit.merchant.customer_ui.ui.transaction_details._di.TransactionFragmentModule
import `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount.UpdateTransactionAmountScreen
import `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount._di.UpdateTransactionAmountModule
import `in`.okcredit.merchant.profile.BusinessFragment
import `in`.okcredit.onboarding.autolang.AutoLangFragment
import `in`.okcredit.onboarding.businessname.BusinessNameFragment
import `in`.okcredit.onboarding.businessname.BusinessNameModule
import `in`.okcredit.onboarding.change_number.ChangeNumberFragment
import `in`.okcredit.onboarding.change_number.ChangeNumberModule
import `in`.okcredit.onboarding.enterotp.EnterOtpFragment
import `in`.okcredit.onboarding.otp_verification.OtpVerificationFragment
import `in`.okcredit.payment.ui.add_payment_dialog.AddPaymentDestinationDialog
import `in`.okcredit.payment.ui.add_payment_dialog._di.AddPaymentDestinationModule
import `in`.okcredit.payment.ui.blindpay.BlindPayDialog
import `in`.okcredit.payment.ui.blindpay.di.BlindPayModule
import `in`.okcredit.shared.calculator.CalculatorLayout
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderScreen
import `in`.okcredit.shared.dialogs.bottomsheetloader._di.BottomSheetLoaderModule
import `in`.okcredit.shared.mini_calculator.MiniCalculatorLayout
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import merchant.okcredit.accounting.ui.transaction_sort_options_dialog.TransactionsSortCriteriaSelectionBottomSheet
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.feedback.feedback.FeedbackFragment
import tech.okcredit.feedback.feedback._di.FeedbackModule
import tech.okcredit.help.helpHome.HelpHomeFragment
import tech.okcredit.help.helpHome.di.HelpHomeModule
import tech.okcredit.help.help_details.HelpDetailsFragment
import tech.okcredit.help.help_details._di.HelpDetailsModule
import tech.okcredit.help.help_main.HelpFragment
import tech.okcredit.help.help_main._di.HelpFragmentModule
import tech.okcredit.help.helpcontactus.HelpContactUsFragment
import tech.okcredit.help.helpcontactus._di.HelpContactUsModule
import tech.okcredit.home.ui._di.HomeModule

@Module(
    includes = [
        HomeModule::class,
        CollectionsHomeActivityModule::class
    ]
)
abstract class MainActivityModule {
    @FragmentScope
    @ContributesAndroidInjector(modules = [CustomerFragmentModule::class])
    abstract fun customerFragment(): CustomerFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [CollectWithGooglePayModule::class])
    abstract fun collectWithGooglePayBottomSheet(): CollectWithGooglePayBottomSheet

    @FragmentScope
    @ContributesAndroidInjector(modules = [LiveSalesFragmentModule::class])
    abstract fun liveSalesFragment(): LiveSalesFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [TransactionFragmentModule::class])
    abstract fun transactionFragment(): TransactionFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [PrivacyFragmentModule::class])
    abstract fun privacyFragment(): PrivacyFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [HelpFragmentModule::class])
    abstract fun helpFragment(): HelpFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [HelpHomeModule::class])
    abstract fun helpHomeFragment(): HelpHomeFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [HelpDetailsModule::class])
    abstract fun helpDetailsFragment(): HelpDetailsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [HelpContactUsModule::class])
    abstract fun helpContactUsFragment(): HelpContactUsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [FeedbackModule::class])
    abstract fun feedbackFragment(): FeedbackFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [PasswordEnableFragmentModule::class])
    abstract fun passwordEnableFragment(): PasswordEnableFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [AccountStatementModule::class])
    abstract fun accountStatementFragment(): AccountStatementFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [EnterOtpFragmentModule::class])
    abstract fun enterOtpFragment(): EnterOtpFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [SupplierTransactionFragmentModule::class])
    abstract fun supplierTransactionFragment(): SupplierTransactionFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [OtpVerificationFragmentModule::class])
    abstract fun otpVerificationFragment(): OtpVerificationFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [DueCustomerModule::class])
    abstract fun dueCustomerFragment(): DueCustomerFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [InfoChangeNumberModule::class])
    abstract fun infoChangeNumberFragment(): InfoChangeNumberFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ConfirmNumberChangeModule::class])
    abstract fun confirmNumberChangeFragment(): ConfirmNumberChangeFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ChangeNumberModule::class])
    abstract fun newNumberFragment(): ChangeNumberFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [SyncModule::class])
    abstract fun syncFragment(): SyncFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [MerchantFragmentModule::class])
    abstract fun merchantFragment(): BusinessFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [AutoLangModule::class])
    abstract fun autoLangFragment(): AutoLangFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [KnowMoreModule::class])
    abstract fun knowMoreFragment(): KnowMoreFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [BusinessNameModule::class])
    abstract fun businessNameFragment(): BusinessNameFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun voiceInputBottomSheetFragment(): VoiceInputBottomSheetFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [CategoryFragmentModule::class])
    abstract fun categoryFragment(): CategoryFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [MoveToSupplierModule::class])
    abstract fun moveToSupplierFragment(): MoveToSupplierFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [DiscountDetailsFragmentModule::class])
    abstract fun discountDetailsFragment(): DiscountDetailsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [AddDiscountFragmentModule::class])
    abstract fun addDiscountFragment(): AddDiscountFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ExpenseManagerModule::class])
    abstract fun expenseManagerFragment(): ExpenseManagerFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [AddExpenseModule::class])
    abstract fun addExpenseFragment(): AddExpenseFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [AddPaymentDestinationModule::class])
    abstract fun addPaymentDialog(): AddPaymentDestinationDialog

    @Binds
    abstract fun activity(activity: MainActivity): AppCompatActivity

    @FragmentScope
    @ContributesAndroidInjector(modules = [AddNumberDialogModule::class])
    abstract fun addMobileNumberDialog(): AddNumberDialogScreen

    @FragmentScope
    @ContributesAndroidInjector(modules = [UpdateTransactionAmountModule::class])
    abstract fun updateTransactionAmount(): UpdateTransactionAmountScreen

    @FragmentScope
    @ContributesAndroidInjector(modules = [CustomerReportsModule::class])
    abstract fun customerReport(): CustomerReportsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [CustomerTxnAlertDialogModule::class])
    abstract fun customerTxnAlertDialogFragment(): CustomerTxnAlertDialogScreen

    @FragmentScope
    @ContributesAndroidInjector(modules = [MenuOptionBottomSheetModule::class])
    abstract fun menuOptionsBottomSheet(): MenuOptionsBottomSheet

    @FragmentScope
    @ContributesAndroidInjector(modules = [SupplierReportsModule::class])
    abstract fun supplierReport(): SupplierReportsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [CustomerAddTxnDiscountInfoDialogModule::class])
    abstract fun customerAddTxnDiscountInfo(): CustomerAddTxnDiscountInfoDialogScreen

    @FragmentScope
    @ContributesAndroidInjector(modules = [BottomSheetLoaderModule::class])
    abstract fun bottomSheetLoader(): BottomSheetLoaderScreen

    @ContributesAndroidInjector
    abstract fun calculatorLayout(): CalculatorLayout

    @ContributesAndroidInjector
    abstract fun miniCalculatorLayout(): MiniCalculatorLayout

    @ContributesAndroidInjector(modules = [BlindPayModule::class])
    abstract fun blindPayDialog(): BlindPayDialog

    @ContributesAndroidInjector
    abstract fun transactionsSortCriteriaSelectionBottomSheet(): TransactionsSortCriteriaSelectionBottomSheet
}
