package `in`.okcredit.frontend.ui.enter_otp._di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.onboarding.contract.OnboardingConstants
import `in`.okcredit.onboarding.enterotp.EnterOtpContract
import `in`.okcredit.onboarding.enterotp.EnterOtpFragment
import `in`.okcredit.onboarding.enterotp.EnterOtpViewModel
import `in`.okcredit.onboarding.otp_verification.OtpArgs.ARG_FLAG
import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.shared.utils.SmsHelper
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class EnterOtpFragmentModule {

    companion object {

        @Provides
        fun smsHelper(activity: MainActivity): SmsHelper {
            return SmsHelper(activity)
        }

        @Provides
        @ViewModelParam(OnboardingConstants.ARG_MOBILE)
        fun mobile(activity: MainActivity): String {
            return activity.intent.getStringExtra(OnboardingConstants.ARG_MOBILE)
        }

        @Provides
        @ViewModelParam("flag")
        fun otpData(activity: MainActivity): Int {
            return activity.intent.getIntExtra(ARG_FLAG, 0)
        }

        @Provides
        @ViewModelParam("arg_sign_out_from_all_devices")
        fun provideSignOutInfo(activity: MainActivity): Boolean {
            return activity.intent.getBooleanExtra(MainActivity.ARG_SIGN_OUT_ALL_DEVICES, false)
        }

        @Provides
        @ViewModelParam(OnboardingConstants.ARG_GOOGLE_AUTO_READ_MOBILE_NUMBER)
        fun googlePopupSelected(activity: MainActivity): Boolean {
            return activity.intent.getBooleanExtra(OnboardingConstants.ARG_GOOGLE_AUTO_READ_MOBILE_NUMBER, false)
        }

        @Provides
        fun initialState(): EnterOtpContract.State = EnterOtpContract.State()

        @Provides
        fun viewModel(
            fragment: EnterOtpFragment,
            viewModelProvider: Provider<EnterOtpViewModel>
        ): MviViewModel<EnterOtpContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
