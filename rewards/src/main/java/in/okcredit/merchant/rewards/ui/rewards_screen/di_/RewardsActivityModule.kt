package `in`.okcredit.merchant.rewards.ui.rewards_screen.di_

import `in`.okcredit.merchant.rewards.ui.rewards_screen.RewardsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class RewardsActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [RewardsFragmentModule::class])
    abstract fun rewardsScreen(): RewardsFragment
}
