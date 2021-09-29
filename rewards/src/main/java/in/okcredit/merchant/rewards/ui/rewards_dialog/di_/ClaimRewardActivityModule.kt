package `in`.okcredit.merchant.rewards.ui.rewards_dialog.di_

import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardActivity
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardsDialog
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class ClaimRewardActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [ClaimRewardsFragmentModule::class])
    abstract fun claimRewardsDialog(): ClaimRewardsDialog

    @Binds
    abstract fun activity(activity: ClaimRewardActivity): AppCompatActivity
}
