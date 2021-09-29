package `in`.okcredit.frontend.ui.otp_verification._di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.onboarding.otp_verification.OtpArgs.ARG_FLAG
import `in`.okcredit.onboarding.otp_verification.OtpVerificationContract
import `in`.okcredit.onboarding.otp_verification.OtpVerificationFragment
import `in`.okcredit.onboarding.otp_verification.OtpVerificationViewModel
import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.shared.utils.SmsHelper
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class OtpVerificationFragmentModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: OtpVerificationFragment): OtpVerificationContract.Navigator

    companion object {

        @Provides
        fun smsHelper(activity: MainActivity): SmsHelper {
            return SmsHelper(activity)
        }

        @Provides
        @ViewModelParam("flag")
        fun otpData(activity: MainActivity): Int {
            return activity.intent.getIntExtra(ARG_FLAG, 0)
        }

        @Provides
        fun initialState(): OtpVerificationContract.State = OtpVerificationContract.State()

        @Provides
        fun viewModel(
            fragment: OtpVerificationFragment,
            viewModelProvider: Provider<OtpVerificationViewModel>
        ): MviViewModel<OtpVerificationContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
