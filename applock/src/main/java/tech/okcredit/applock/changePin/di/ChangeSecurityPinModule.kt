package tech.okcredit.applock.changePin.di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.shared.utils.SmsHelper
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.applock.AppLockActivityV2.Companion.ENTRY
import tech.okcredit.applock.changePin.ChangeSecurityPinContract.State
import tech.okcredit.applock.changePin.ChangeSecurityPinFragment
import tech.okcredit.applock.changePin.ChangeSecurityPinFragment.Companion.Source
import tech.okcredit.applock.changePin.ChangeSecurityPinViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class ChangeSecurityPinModule {

    companion object {

        @Provides
        fun smsHelper(fragment: ChangeSecurityPinFragment): SmsHelper {
            return SmsHelper(fragment.requireActivity())
        }

        @Provides
        @ViewModelParam(Source)
        fun screen(fragment: ChangeSecurityPinFragment): String {
            return fragment.activity?.intent?.getStringExtra(Source) ?: ""
        }

        @Provides
        @ViewModelParam(ENTRY)
        @JvmStatic
        fun entry(fragment: ChangeSecurityPinFragment): String {
            return fragment.activity?.intent?.getStringExtra(ENTRY) ?: ""
        }

        @Provides
        fun initialState(): State = State()

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: ChangeSecurityPinFragment,
            viewModelProvider: Provider<ChangeSecurityPinViewModel>
        ): MviViewModel<State> = fragment.createViewModel(viewModelProvider)
    }
}
