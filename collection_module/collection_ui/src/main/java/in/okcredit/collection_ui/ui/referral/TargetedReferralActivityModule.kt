package `in`.okcredit.collection_ui.ui.referral

import `in`.okcredit.collection_ui.ui.referral.education.ReferralEducationFragment
import `in`.okcredit.collection_ui.ui.referral.education.di.ReferralEducationModule
import `in`.okcredit.collection_ui.ui.referral.invite_list.ReferralInviteListFragment
import `in`.okcredit.collection_ui.ui.referral.invite_list.di.ReferralInviteListModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TargetedReferralActivityModule {

    @ContributesAndroidInjector(modules = [ReferralEducationModule::class])
    abstract fun referralEducationScreen(): ReferralEducationFragment

    @ContributesAndroidInjector(modules = [ReferralInviteListModule::class])
    abstract fun referralInviteListFragment(): ReferralInviteListFragment
}
