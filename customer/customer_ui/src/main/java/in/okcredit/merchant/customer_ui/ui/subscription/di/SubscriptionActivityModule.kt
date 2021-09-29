package `in`.okcredit.merchant.customer_ui.ui.subscription.di

import `in`.okcredit.merchant.customer_ui.ui.subscription.add.AddSubscriptionFragment
import `in`.okcredit.merchant.customer_ui.ui.subscription.add.AddSubscriptionFragmentModule
import `in`.okcredit.merchant.customer_ui.ui.subscription.detail.SubscriptionDetailFragment
import `in`.okcredit.merchant.customer_ui.ui.subscription.detail.SubscriptionDetailFragmentModule
import `in`.okcredit.merchant.customer_ui.ui.subscription.list.SubscriptionListFragment
import `in`.okcredit.merchant.customer_ui.ui.subscription.list.SubscriptionListFragmentModule
import `in`.okcredit.shared.calculator.CalculatorLayout
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderScreen
import `in`.okcredit.shared.dialogs.bottomsheetloader._di.BottomSheetLoaderModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SubscriptionActivityModule {

    @ContributesAndroidInjector(modules = [SubscriptionListFragmentModule::class])
    abstract fun subscriptionListScreen(): SubscriptionListFragment

    @ContributesAndroidInjector(modules = [SubscriptionDetailFragmentModule::class])
    abstract fun subscriptionDetailScreen(): SubscriptionDetailFragment

    @ContributesAndroidInjector(modules = [AddSubscriptionFragmentModule::class])
    abstract fun addSubscriptionScreen(): AddSubscriptionFragment

    @ContributesAndroidInjector(modules = [BottomSheetLoaderModule::class])
    abstract fun bottomSheetLoaderScreen(): BottomSheetLoaderScreen

    @ContributesAndroidInjector
    abstract fun calculatorLayout(): CalculatorLayout
}
