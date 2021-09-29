package `in`.okcredit.ui.reset_pwd._di

import `in`.okcredit.ui.reset_pwd.otp.OtpFragment
import `in`.okcredit.ui.reset_pwd.password.PasswordFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ResetPasswordActivityFragmentModule {

    @ContributesAndroidInjector(modules = [OtpFragmentModule::class])
    abstract fun otpFragment(): OtpFragment

    @ContributesAndroidInjector(modules = [PasswordFragmentModule::class])
    abstract fun passwordFragment(): PasswordFragment
}
