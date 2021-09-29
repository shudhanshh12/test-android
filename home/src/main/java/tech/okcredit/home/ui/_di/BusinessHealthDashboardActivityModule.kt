package tech.okcredit.home.ui._di

import `in`.okcredit.backend._id.scope.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardFragment

@Module
abstract class BusinessHealthDashboardActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [BusinessHealthDashboardUiModule::class])
    abstract fun businessHealthDashboardFragment(): BusinessHealthDashboardFragment
}
