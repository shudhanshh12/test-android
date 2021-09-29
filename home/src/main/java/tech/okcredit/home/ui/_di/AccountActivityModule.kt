package tech.okcredit.home.ui._di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.home.ui.acccountV2.di.AccountModule
import tech.okcredit.home.ui.acccountV2.ui.AccountFragment

@Module
abstract class AccountActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [AccountModule::class])
    abstract fun accountScreen(): AccountFragment
}
