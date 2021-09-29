package `in`.okcredit.merchant.ui.di

import `in`.okcredit.merchant.ui.create_business.CreateBusinessDialog
import `in`.okcredit.merchant.ui.create_business.di.CreateBusinessModule
import `in`.okcredit.merchant.ui.select_business.SelectBusinessFragment
import `in`.okcredit.merchant.ui.select_business.di.SelectBusinessModule
import `in`.okcredit.merchant.ui.switch_business.SwitchBusinessDialog
import `in`.okcredit.merchant.ui.switch_business.di.SwitchBusinessModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BusinessUiModule {

    @ContributesAndroidInjector(modules = [SwitchBusinessModule::class])
    abstract fun switchBusinessDialog(): SwitchBusinessDialog

    @ContributesAndroidInjector(modules = [CreateBusinessModule::class])
    abstract fun createBusinessDialog(): CreateBusinessDialog

    @ContributesAndroidInjector(modules = [SelectBusinessModule::class])
    abstract fun selectBusinessFragment(): SelectBusinessFragment
}
