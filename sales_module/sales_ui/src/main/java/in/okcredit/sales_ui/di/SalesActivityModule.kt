package `in`.okcredit.sales_ui.di

import `in`.okcredit.sales_ui.SalesActivity
import `in`.okcredit.sales_ui.ui.add_bill_dialog.AddBillBottomSheetDialog
import `in`.okcredit.sales_ui.ui.add_bill_dialog._di.AddBillDialogModule
import `in`.okcredit.sales_ui.ui.add_bill_items.AddBillItemsFragment
import `in`.okcredit.sales_ui.ui.add_bill_items._di.AddBillItemModule
import `in`.okcredit.sales_ui.ui.add_sales.AddSaleFragment
import `in`.okcredit.sales_ui.ui.add_sales._di.AddSaleModule
import `in`.okcredit.sales_ui.ui.bill_summary.BillSummaryFragment
import `in`.okcredit.sales_ui.ui.bill_summary._di.BillSummaryModule
import `in`.okcredit.sales_ui.ui.billing_name.BillingNameBottomSheetDialog
import `in`.okcredit.sales_ui.ui.billing_name._di.BillingNameModule
import `in`.okcredit.sales_ui.ui.list_sales.SalesOnCashFragment
import `in`.okcredit.sales_ui.ui.list_sales._di.SalesOnCashModule
import `in`.okcredit.sales_ui.ui.view_sale.SalesDetailFragment
import `in`.okcredit.sales_ui.ui.view_sale._di.SalesDetailModule
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class SalesActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [SalesOnCashModule::class])
    abstract fun salesOnCashScreen(): SalesOnCashFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [SalesDetailModule::class])
    abstract fun salesDetailScreen(): SalesDetailFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [BillingNameModule::class])
    abstract fun billingNameDialog(): BillingNameBottomSheetDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [AddSaleModule::class])
    abstract fun addSaleScreen(): AddSaleFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [BillSummaryModule::class])
    abstract fun billSummaryScreen(): BillSummaryFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [AddBillItemModule::class])
    abstract fun addBillItemsScreen(): AddBillItemsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [AddBillDialogModule::class])
    abstract fun addBillDialog(): AddBillBottomSheetDialog

    @Binds
    abstract fun activity(activity: SalesActivity): AppCompatActivity
}
