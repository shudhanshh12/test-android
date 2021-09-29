package tech.okcredit.home.ui._di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialog
import tech.okcredit.home.dialogs.customer_profile_dialog._di.CustomerProfileDialogModule
import tech.okcredit.home.ui.homesearch.HomeSearchFragment

@Module
abstract class HomeSearchActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [HomeSearchModule::class])
    abstract fun homeSearchScreen(): HomeSearchFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [CustomerProfileDialogModule::class])
    abstract fun customerProfileDialog(): CustomerProfileDialog
}
