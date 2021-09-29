package merchant.okcredit.gamification.ipl.rewards._di

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardActivity
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardFragment
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.ClaimMysteryPrizeFragment
import merchant.okcredit.gamification.ipl.rewards.mysteryprize._di.ClaimMysteryPrizeFragmentModule

@Module
abstract class ClaimRewardActivityModule {

    @ContributesAndroidInjector(modules = [ClaimRewardFragmentModule::class])
    abstract fun claimRewardFragment(): ClaimRewardFragment

    @ContributesAndroidInjector(modules = [ClaimMysteryPrizeFragmentModule::class])
    abstract fun claimMysteryPrizeFragment(): ClaimMysteryPrizeFragment

    @Binds
    abstract fun activity(activity: ClaimRewardActivity): AppCompatActivity
}
