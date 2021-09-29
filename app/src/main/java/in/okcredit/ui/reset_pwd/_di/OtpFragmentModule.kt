package `in`.okcredit.ui.reset_pwd._di

import `in`.okcredit.ui.reset_pwd.otp.OtpContract
import `in`.okcredit.ui.reset_pwd.otp.OtpFragment
import `in`.okcredit.ui.reset_pwd.otp.OtpPresenter
import `in`.okcredit.ui.reset_pwd.otp.SmsHelper
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam

@Module
abstract class OtpFragmentModule {

    @Binds
    abstract fun viewModel(viewModel: OtpPresenter): OtpContract.Presenter

    companion object {

        @Provides
        @ViewModelParam("mobile")
        fun mobile(fragment: OtpFragment) = fragment.requireArguments().getString(OtpFragment.ARG_MOBILE)!!

        @Provides
        fun smsHelper(fragment: OtpFragment) = SmsHelper(fragment.activity)
    }
}
