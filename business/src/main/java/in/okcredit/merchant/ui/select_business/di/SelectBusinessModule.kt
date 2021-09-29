package `in`.okcredit.merchant.ui.select_business.di

import `in`.okcredit.merchant.ui.select_business.SelectBusinessContract
import `in`.okcredit.merchant.ui.select_business.SelectBusinessFragment
import `in`.okcredit.merchant.ui.select_business.SelectBusinessViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class SelectBusinessModule {

    companion object {

        @Provides
        fun initialState(): SelectBusinessContract.State = SelectBusinessContract.State()

        @Provides
        fun viewModel(
            fragment: SelectBusinessFragment,
            provider: Provider<SelectBusinessViewModel>,
        ): MviViewModel<SelectBusinessContract.State> = fragment.createViewModel(provider)
    }
}
