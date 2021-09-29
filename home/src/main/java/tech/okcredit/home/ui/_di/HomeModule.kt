package tech.okcredit.home.ui._di

import `in`.okcredit.dynamicview.di.DynamicViewModule
import `in`.okcredit.onboarding.language.views.LanguageBottomSheet
import `in`.okcredit.web_features.cash_counter.CashCounterFragment
import `in`.okcredit.web_features.cash_counter._di.CashCounterModule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.home.dialogs.AddTransactionShortcutRequestBottomSheet
import tech.okcredit.home.dialogs.MerchantAddressRequestBottomSheetDialog
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialog
import tech.okcredit.home.dialogs.customer_profile_dialog._di.CustomerProfileDialogModule
import tech.okcredit.home.dialogs.supplier_payment_dialog.SupplierPaymentDialog
import tech.okcredit.home.dialogs.supplier_payment_dialog._di.SupplierPaymentModule
import tech.okcredit.home.ui.acccountV2.di.AccountModule
import tech.okcredit.home.ui.acccountV2.ui.AccountFragment
import tech.okcredit.home.ui.customer_tab.CustomerTabFragment
import tech.okcredit.home.ui.dashboard.DashboardFragment
import tech.okcredit.home.ui.home.HomeFragment
import tech.okcredit.home.ui.home.dialog.AddBankDetailBottomSheet
import tech.okcredit.home.ui.home.supplier.SupplierSortFragment
import tech.okcredit.home.ui.home.supplier.SupplierSortModule
import tech.okcredit.home.ui.menu.HomeMenuFragment
import tech.okcredit.home.ui.reminder.bulk.BulkReminderBottomSheet
import tech.okcredit.home.ui.reminder.bulk.BulkReminderModule
import tech.okcredit.home.ui.supplier_tab.SupplierTabFragment

@Module(includes = [DynamicViewModule::class])
abstract class HomeModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [HomeFragmentModule::class])
    abstract fun homeFragment(): HomeFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [CustomerTabFragmentModule::class])
    abstract fun customerTabFragment(): CustomerTabFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [SupplierTabFragmentModule::class])
    abstract fun supplierTabFragment(): SupplierTabFragment

    @ContributesAndroidInjector(modules = [AccountModule::class])
    abstract fun accountScreen(): AccountFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun merchantAddressRequestBottomSheetDialog(): MerchantAddressRequestBottomSheetDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [CustomerProfileDialogModule::class])
    abstract fun customerProfileDialogDialog(): CustomerProfileDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [DashboardModule::class])
    abstract fun dashboardScreen(): DashboardFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [HomeMenuFragmentModule::class])
    abstract fun menuScreen(): HomeMenuFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [CashCounterModule::class])
    abstract fun cashCounterScreen(): CashCounterFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun addTransactionShortcutRequestBottomSheet(): AddTransactionShortcutRequestBottomSheet

    @ContributesAndroidInjector(modules = [SupplierSortModule::class])
    abstract fun supplierSortBottomSheetFragment(): SupplierSortFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [SupplierPaymentModule::class])
    abstract fun supplierPaymentDialog(): SupplierPaymentDialog

    @ContributesAndroidInjector(modules = [BulkReminderModule::class])
    abstract fun bulkReminderBottomSheet(): BulkReminderBottomSheet

    @ContributesAndroidInjector
    abstract fun languageBottomSheet(): LanguageBottomSheet

    @ContributesAndroidInjector
    abstract fun addBankDetailBottomSheet(): AddBankDetailBottomSheet
}
