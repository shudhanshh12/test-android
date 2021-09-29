package `in`.okcredit.frontend.ui.onboarding

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.onboarding.autolang.AutoLangContract
import `in`.okcredit.onboarding.autolang.AutoLangFragment
import `in`.okcredit.onboarding.autolang.AutoLangViewModel
import `in`.okcredit.onboarding.contract.OnboardingConstants
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AutoLangModule {

    companion object {

        @Provides
        fun initialState(): AutoLangContract.State = AutoLangContract.State()

        @Provides
        @ViewModelParam(OnboardingConstants.ARG_MOBILE)
        fun mobile(activity: MainActivity): String {
            return activity.intent.getStringExtra(OnboardingConstants.ARG_MOBILE) ?: ""
        }

        @Provides
        fun viewModel(
            fragment: AutoLangFragment,
            viewModelProvider: Provider<AutoLangViewModel>
        ): MviViewModel<AutoLangContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
