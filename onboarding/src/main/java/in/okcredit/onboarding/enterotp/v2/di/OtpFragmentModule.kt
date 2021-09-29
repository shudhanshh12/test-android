package `in`.okcredit.onboarding.enterotp.v2.di

import `in`.okcredit.onboarding.enterotp.v2.OtpContractV2
import `in`.okcredit.onboarding.enterotp.v2.OtpV2Fragment
import `in`.okcredit.onboarding.enterotp.v2.OtpV2FragmentArgs
import `in`.okcredit.onboarding.enterotp.v2.OtpV2ViewModel
import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.shared.utils.SmsHelper
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class OtpFragmentModule {

    companion object {

        @Provides
        fun initialState(): OtpContractV2.State = OtpContractV2.State()

        @Provides
        fun smsHelper(activity: AppCompatActivity) = SmsHelper(activity)

        @Provides
        fun arguments(fragment: OtpV2Fragment) = OtpV2FragmentArgs.fromBundle(fragment.requireArguments())

        @Provides
        fun viewModel(
            fragment: OtpV2Fragment,
            providerViewModel: Provider<OtpV2ViewModel>
        ): MviViewModel<OtpContractV2.State> = fragment.createViewModel(providerViewModel)
    }
}
