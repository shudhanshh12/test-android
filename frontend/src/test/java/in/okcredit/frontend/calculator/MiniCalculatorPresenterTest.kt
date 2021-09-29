package `in`.okcredit.frontend.calculator

import `in`.okcredit.shared.mini_calculator.MiniCalculatorContract
import `in`.okcredit.shared.mini_calculator.MiniCalculatorViewModel
import com.google.common.truth.Truth.assertThat
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Test

class MiniCalculatorPresenterTest {
    private lateinit var viewModel: MiniCalculatorViewModel

    private fun createViewModel(initialState: MiniCalculatorContract.State) {
        viewModel = MiniCalculatorViewModel(
            initialState = initialState
        )
    }

    @Test
    fun `calculator operation 2`() { // d means . in this math expression
        // setup
        val initialState = MiniCalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<MiniCalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(MiniCalculatorContract.Intent.OnDigitClicked(2)))

        val finalExpectedState = initialState.copy(
            amount = 2L
        )
        // expectations
        assertThat(
            testObserver.values().last() == finalExpectedState
        )
    }

    @Test
    fun `calculator operation 2 dot 3`() { // d means . in this math expression
        // setup
        val initialState = MiniCalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<MiniCalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(MiniCalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(MiniCalculatorContract.Intent.OnDotClicked))
        viewModel.attachIntents(Observable.just(MiniCalculatorContract.Intent.OnDigitClicked(3)))

        val finalExpectedState = initialState.copy(
            amount = 230L
        )
        // expectations
        assertThat(
            testObserver.values().last() == finalExpectedState
        )
    }

    @Test
    fun `calculator amount calulation empty`() { // d means . in this math expression
        // setup
        val initialState = MiniCalculatorContract.State()
        val newState = initialState.copy(amountCalculation = "")

        // create Presenter
        createViewModel(newState)

        // observe state
        val testObserver = TestObserver<MiniCalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        val finalExpectedState = newState
        // expectations
        assertThat(
            testObserver.values().last() == finalExpectedState
        )
    }

    @Test
    fun `calculator operation -0L`() { // d means . in this math expression
        // setup
        val initialState = MiniCalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<MiniCalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(MiniCalculatorContract.Intent.OnDigitClicked(-0)))

        val finalExpectedState = initialState.copy(
            amount = 0L
        )
        // expectations
        assertThat(
            testObserver.values().last() == finalExpectedState
        )
    }

    @Test
    fun `calculator operation amount zero and amountCalculation not zero`() { // d means . in this math expression
        // setup
        val initialState = MiniCalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<MiniCalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(MiniCalculatorContract.Intent.OnDotClicked))

        viewModel.attachIntents(Observable.just(MiniCalculatorContract.Intent.OnDigitClicked(0)))

        val finalExpectedState = initialState.copy(
            amount = 0L
        )
        // expectations
        assertThat(
            testObserver.values().last() == finalExpectedState
        )
    }

    @Test
    fun `calculator operation backspace when difit 2 3`() { // d means . in this math expression
        // setup
        val initialState = MiniCalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<MiniCalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent

        viewModel.attachIntents(Observable.just(MiniCalculatorContract.Intent.OnDigitClicked(2)))
        viewModel.attachIntents(Observable.just(MiniCalculatorContract.Intent.OnDigitClicked(3)))

        viewModel.attachIntents(Observable.just(MiniCalculatorContract.Intent.OnBackSpaceClicked))

        val finalExpectedState = initialState.copy(
            amount = 2L
        )
        // expectations
        assertThat(
            testObserver.values().last() == finalExpectedState
        )
    }

    @Test
    fun `calculator operation backspace when amount 0`() { // d means . in this math expression
        // setup
        val initialState = MiniCalculatorContract.State()

        // create Presenter
        createViewModel(initialState)

        // observe state
        val testObserver = TestObserver<MiniCalculatorContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent

        viewModel.attachIntents(Observable.just(MiniCalculatorContract.Intent.OnBackSpaceClicked))

        val finalExpectedState = initialState
        // expectations
        assertThat(
            testObserver.values().last() == finalExpectedState
        )
    }
}
