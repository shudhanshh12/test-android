package `in`.okcredit.web_features.cash_counter._di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.web_features.cash_counter.CashCounterContract
import `in`.okcredit.web_features.cash_counter.CashCounterFragment
import `in`.okcredit.web_features.cash_counter.CashCounterViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class CashCounterModule {

    companion object {

        @Provides
        fun initialState(): CashCounterContract.State = CashCounterContract.State()

        @Provides
        fun viewModel(
            fragment: CashCounterFragment,
            viewModelProvider: Provider<CashCounterViewModel>
        ): MviViewModel<CashCounterContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
