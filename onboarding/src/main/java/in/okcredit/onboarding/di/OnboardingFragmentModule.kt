package `in`.okcredit.onboarding.di

import `in`.okcredit.onboarding.businessname.BusinessNameFragment
import `in`.okcredit.onboarding.businessname.BusinessNameModule
import `in`.okcredit.onboarding.enterotp.v2.OtpV2Fragment
import `in`.okcredit.onboarding.enterotp.v2.di.OtpFragmentModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OnboardingFragmentModule {

    @ContributesAndroidInjector(modules = [OtpFragmentModule::class])
    abstract fun otpV2Fragment(): OtpV2Fragment

    @ContributesAndroidInjector(modules = [BusinessNameModule::class])
    abstract fun businessNameFragment(): BusinessNameFragment
}
