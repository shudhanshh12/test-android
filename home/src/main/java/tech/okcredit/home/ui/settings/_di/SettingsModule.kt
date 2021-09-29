package tech.okcredit.home.ui.settings._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.home.ui.settings.SettingsContract
import tech.okcredit.home.ui.settings.SettingsFragment
import tech.okcredit.home.ui.settings.SettingsViewModel
import javax.inject.Provider

@Module
abstract class SettingsModule {

    companion object {
        @Provides
        fun initialState(): SettingsContract.State = SettingsContract.State()

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: SettingsFragment,
            viewModelProvider: Provider<SettingsViewModel>
        ): MviViewModel<SettingsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
