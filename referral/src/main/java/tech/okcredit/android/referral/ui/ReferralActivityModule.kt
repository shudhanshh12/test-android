package tech.okcredit.android.referral.ui

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.android.referral.di.ShareReferralFragmentModule
import tech.okcredit.android.referral.ui.know_more.ReferralKnowMoreFragment
import tech.okcredit.android.referral.ui.know_more.di_.ReferralKnowMoreFragmentModule
import tech.okcredit.android.referral.ui.referral_rewards_v1.ReferralRewardsFragment
import tech.okcredit.android.referral.ui.referral_rewards_v1.di_.ReferralRewardsModule
import tech.okcredit.android.referral.ui.referral_screen.ReferralFragment
import tech.okcredit.android.referral.ui.referral_screen.di_.ReferralFragmentModule
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListFragment
import tech.okcredit.android.referral.ui.referral_target_user_list.di_.TargetedUserListFragmentModule
import tech.okcredit.android.referral.ui.share.ShareReferralFragment
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class ReferralActivityModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [TargetedUserListFragmentModule::class])
    abstract fun referralTargetedUsersListScreen(): ReferralTargetedUsersListFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ReferralFragmentModule::class])
    abstract fun referralScreen(): ReferralFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ReferralKnowMoreFragmentModule::class])
    abstract fun referralKnowMoreFragment(): ReferralKnowMoreFragment

    @ContributesAndroidInjector(modules = [ReferralRewardsModule::class])
    abstract fun referredMerchantListScreen(): ReferralRewardsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ShareReferralFragmentModule::class])
    abstract fun shareReferralFragment(): ShareReferralFragment

    @Binds
    abstract fun activity(activity: ReferralActivity): AppCompatActivity
}
