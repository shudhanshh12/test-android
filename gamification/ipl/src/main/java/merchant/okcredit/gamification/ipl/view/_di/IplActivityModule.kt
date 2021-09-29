package merchant.okcredit.gamification.ipl.view._di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardFragment
import merchant.okcredit.gamification.ipl.leaderboard._di.LeaderboardFragmentModule
import merchant.okcredit.gamification.ipl.match.SelectMatchFragment
import merchant.okcredit.gamification.ipl.match._di.SelectMatchModule
import merchant.okcredit.gamification.ipl.sundaygame.SundayGameFragment
import merchant.okcredit.gamification.ipl.sundaygame._di.SundayGameFragmentModule
import merchant.okcredit.gamification.ipl.view.IplFragment

@Module
abstract class IplActivityModule {

    @ContributesAndroidInjector(modules = [IplFragmentModule::class])
    abstract fun iplFragment(): IplFragment

    @ContributesAndroidInjector(modules = [SelectMatchModule::class])
    abstract fun selectMatchFragment(): SelectMatchFragment

    @ContributesAndroidInjector(modules = [LeaderboardFragmentModule::class])
    abstract fun leaderboardFragment(): LeaderboardFragment

    @ContributesAndroidInjector(modules = [SundayGameFragmentModule::class])
    abstract fun sundayGameFragment(): SundayGameFragment
}
