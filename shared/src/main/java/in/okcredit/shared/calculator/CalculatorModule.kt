package `in`.okcredit.shared.calculator

import `in`.okcredit.shared.base.IBaseLayoutViewModel
import dagger.Module
import dagger.Provides

@Module
abstract class CalculatorModule {

    companion object {

        @Provides
        fun initialState() = CalculatorContract.State()

        @Provides
        fun sendMessagePresenter(
            calculatorPresenter: CalculatorViewModel
        ): IBaseLayoutViewModel<CalculatorContract.State> {
            return calculatorPresenter
        }
    }
}
