package tech.okcredit.home.ui._di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.home.ui.settings.SettingsFragment
import tech.okcredit.home.ui.settings._di.SettingsModule

@Module
abstract class SettingsActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [SettingsModule::class])
    abstract fun settingsScreen(): SettingsFragment
}
