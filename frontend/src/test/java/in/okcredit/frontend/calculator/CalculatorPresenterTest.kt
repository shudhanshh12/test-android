package `in`.okcredit.frontend.calculator

import `in`.okcredit.shared.calculator.CalculatorContract
import `in`.okcredit.shared.calculator.CalculatorViewModel
import com.google.common.truth.Truth.assertThat
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Test

class CalculatorPresenterTest {
    private lateinit var viewModel: CalculatorViewModel

    private fun createViewModel(initialState: CalculatorContract.State) {
        viewModel = CalculatorViewModel(
            initialState = initialState
        )
    }

    @Test
    fun `calculator operation 2+4-0+35d43+2433`() { // d means . in this math expression
        // setup
        val initialState = CalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<CalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("+")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("-")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(0)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("+")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(5)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDotClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("+")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))

        // expectations
        assertThat(
            testObserver.values().last() == initialState.copy(
                amount = 247443L,
                amountCalculation = "2+4-0+35.43+2433"
            )
        )
    }

    @Test
    fun `calculator operation d82+34d45*23d67`() { // d means . in this math expression
        // setup
        val initialState = CalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<CalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDotClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(8)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("+")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDotClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(5)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("*")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDotClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(6)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(7)))

        // expectations
        assertThat(
            testObserver.values().last() == initialState.copy(
                amount = 81625L,
                amountCalculation = ".82+34.45*23.67"
            )
        )
    }

    @Test
    fun `calculator operation 36b+b53b24+b+3272b+0487*33dbd84`() { // d means '.' in this math expression and b means backspace in this math expression
        // setup
        val initialState = CalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<CalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(6)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnBackSpaceClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("+")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnBackSpaceClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(5)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnBackSpaceClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("+")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnBackSpaceClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("+")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(7)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnBackSpaceClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("+")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(0)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(8)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(7)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("*")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDotClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnBackSpaceClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDotClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(8)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))

        // expectations
        val expectation = testObserver.values()[testObserver.values().size - 1] == initialState.copy(
            amount = 2033108L,
            amountCalculation = "3524+327+0487*33.84"
        )

        assertThat(expectation)
    }

    @Test
    fun `calculator operation 143-34e+34-5d465651`() { // d means '.' in this math expression and b means backspace in this math expression
        // setup
        val initialState = CalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<CalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("-")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnEqualClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("+")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("-")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(5)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDotClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(6)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(7)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(6)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(5)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))

        // expectations
        val expectation =
            testObserver.values().last() == initialState.copy(amount = 13754L, amountCalculation = "109+34-5.46")

        assertThat(expectation)
    }

    @Test
    fun `calculator operation 343-34e+34-5d465651l34`() { // d means . in this math expression and l means long backspace(clear) in this math expression
        // setup
        val initialState = CalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<CalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("-")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnEqualClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("+")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("-")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(5)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDotClicked))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(6)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(7)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(6)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(5)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnLongPressBackSpace))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(3)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(4)))

        // expectations
        val expectation =
            testObserver.values().last() == initialState.copy(amount = 3400L, amountCalculation = "34")

        assertThat(expectation)
    }

    @Test
    fun `calculator operation 111111111111+222222222222`() { // d means . in this math expression
        // setup
        val initialState = CalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<CalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("+")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))

        // expectations
        // expectations
        val expectation =
            testObserver.values().last() == initialState.copy(
                amount = 333333300L,
                amountCalculation = "1111111+2222222",
                amountError = true
            )

        assertThat(expectation)
    }

    @Test
    fun `calculator operation 111-222`() { // d means . in this math expression
        // setup
        val initialState = CalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<CalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(1)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnOperatorClicked("-")))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(CalculatorContract.Intent.OnDigitClicked(2)))

        // expectations
        val expectation =
            testObserver.values().last() == initialState.copy(
                amount = 8900L,
                amountCalculation = "111-22",
                amountError = true
            )

        assertThat(expectation)
    }
}
