package tech.okcredit.applock.pinLock.di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.applock.AppLockActivityV2.Companion.ENTRY
import tech.okcredit.applock.changePin.ChangeSecurityPinFragment.Companion.Source
import tech.okcredit.applock.pinLock.PinLockContract
import tech.okcredit.applock.pinLock.PinLockContract.State
import tech.okcredit.applock.pinLock.PinLockFragment
import tech.okcredit.applock.pinLock.PinLockFragmentArgs
import tech.okcredit.applock.pinLock.PinLockViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class PinLockModule {

    companion object {
        @Provides
        fun initialState(): State = State()

        @Provides
        @ViewModelParam(Source)
        fun source(fragment: PinLockFragment): String {
            return PinLockFragmentArgs.fromBundle(fragment.requireArguments()).source
        }

        @Provides
        @ViewModelParam(ENTRY)
        @JvmStatic
        fun entry(fragment: PinLockFragment): String {
            return fragment.activity?.intent?.getStringExtra(ENTRY) ?: ""
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: PinLockFragment,
            viewModelProvider: Provider<PinLockViewModel>
        ): MviViewModel<PinLockContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
