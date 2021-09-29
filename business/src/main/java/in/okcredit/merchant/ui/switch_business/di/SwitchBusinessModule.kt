package `in`.okcredit.merchant.ui.switch_business.di

import `in`.okcredit.merchant.ui.switch_business.SwitchBusinessContract
import `in`.okcredit.merchant.ui.switch_business.SwitchBusinessDialog
import `in`.okcredit.merchant.ui.switch_business.SwitchBusinessViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class SwitchBusinessModule {

    companion object {

        @Provides
        fun initialState(): SwitchBusinessContract.State = SwitchBusinessContract.State()

        @Provides
        fun viewModel(
            fragment: SwitchBusinessDialog,
            viewModelProvider: Provider<SwitchBusinessViewModel>,
        ): MviViewModel<SwitchBusinessContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
