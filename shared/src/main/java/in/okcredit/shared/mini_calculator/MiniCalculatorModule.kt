package `in`.okcredit.shared.mini_calculator

import `in`.okcredit.shared.base.IBaseLayoutViewModel
import dagger.Module
import dagger.Provides

@Module
abstract class MiniCalculatorModule {

    companion object {

        @Provides
        fun initialState() = MiniCalculatorContract.State()

        @Provides
        fun miniCalaculatorPresenter(
            miniCalculatorPresenter: MiniCalculatorViewModel
        ): IBaseLayoutViewModel<MiniCalculatorContract.State> {
            return miniCalculatorPresenter
        }
    }
}
