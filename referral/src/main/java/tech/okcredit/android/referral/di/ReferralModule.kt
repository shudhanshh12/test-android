package tech.okcredit.android.referral.di

import `in`.okcredit.referral.contract.ReferralNavigator
import `in`.okcredit.referral.contract.RewardsOnSignupTracker
import `in`.okcredit.referral.contract.usecase.CloseReferralTargetBanner
import `in`.okcredit.referral.contract.usecase.GetReferralTarget
import `in`.okcredit.referral.contract.usecase.GetReferralVersion
import `in`.okcredit.referral.contract.usecase.TransactionInitiated
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import tech.okcredit.android.referral.ui.ReferralActivity
import tech.okcredit.android.referral.ui.ReferralActivityModule
import tech.okcredit.android.referral.ui.ReferralNavigatorImpl
import tech.okcredit.android.referral.ui.rewards_on_signup.analytics.RewardsOnSignupTrackerImpl
import tech.okcredit.android.referral.ui.rewards_on_signup.usecase.CloseReferralTargetBannerImpl
import tech.okcredit.android.referral.ui.rewards_on_signup.usecase.GetReferralTargetImpl
import tech.okcredit.android.referral.ui.rewards_on_signup.usecase.TransactionInitiatedImpl
import tech.okcredit.android.referral.ui.share.ShareActivity
import tech.okcredit.android.referral.ui.share.di.ShareActivityModule
import tech.okcredit.android.referral.utils.GetReferralVersionImpl

@Module
abstract class ReferralModule {

    @Binds
    @Reusable
    abstract fun rewardsNavigator(referralNavigatorImpl: ReferralNavigatorImpl): ReferralNavigator

    @Binds
    @Reusable
    abstract fun rewardsOnSignupTracker(rewardsOnSignupTrackerImpl: RewardsOnSignupTrackerImpl): RewardsOnSignupTracker

    @Binds
    @Reusable
    abstract fun getReferralVersion(getReferralVersionImpl: GetReferralVersionImpl): GetReferralVersion

    @Binds
    @Reusable
    abstract fun closeReferralTargetBannerImpl(
        closeReferralTargetBannerImpl: CloseReferralTargetBannerImpl
    ): CloseReferralTargetBanner

    @Binds
    @Reusable
    abstract fun transactionInitiated(
        transactionInitiated: TransactionInitiatedImpl
    ): TransactionInitiated

    @Binds
    @Reusable
    abstract fun getReferralTargets(getReferralTargetImpl: GetReferralTargetImpl): GetReferralTarget

    @ContributesAndroidInjector(modules = [ReferralActivityModule::class])
    abstract fun referralActivity(): ReferralActivity

    @ContributesAndroidInjector(modules = [ShareActivityModule::class])
    abstract fun shareActivity(): ShareActivity
}
