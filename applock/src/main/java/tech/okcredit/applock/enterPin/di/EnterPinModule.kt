package tech.okcredit.applock.enterPin.di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.applock.changePin.ChangeSecurityPinFragment.Companion.Source
import tech.okcredit.applock.enterPin.EnterPinContract
import tech.okcredit.applock.enterPin.EnterPinFragment
import tech.okcredit.applock.enterPin.EnterPinViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
class EnterPinModule {
    companion object {
        @Provides
        fun initialState(): EnterPinContract.State = EnterPinContract.State()

        @Provides
        @ViewModelParam(Source)
        fun screen(fragment: EnterPinFragment): String {
            return fragment.activity?.intent?.getStringExtra(Source) ?: ""
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: EnterPinFragment,
            viewModelProvider: Provider<EnterPinViewModel>
        ): MviViewModel<EnterPinContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
