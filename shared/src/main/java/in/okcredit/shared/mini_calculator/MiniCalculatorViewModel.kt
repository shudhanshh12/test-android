package `in`.okcredit.shared.mini_calculator

import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.BaseLayoutViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.mini_calculator.MiniCalculatorContract.Intent
import `in`.okcredit.shared.mini_calculator.MiniCalculatorContract.PartialState
import `in`.okcredit.shared.utils.MathUtils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MiniCalculatorViewModel
@Inject constructor(
    var initialState: MiniCalculatorContract.State
) :
    BaseLayoutViewModel<MiniCalculatorContract.State, PartialState>(
        initialState,
        Schedulers.newThread(),
        Schedulers.newThread()
    ) {

    private var amount: Long = 0
    private var amountCalculation: String = ""
    private lateinit var interactor: MiniCalculatorContract.Interactor
    private val showInValidAmountPublishSubject: PublishSubject<Unit> = PublishSubject.create()

    override fun handle(): Observable<out UiState.Partial<MiniCalculatorContract.State>> {
        return Observable.mergeArray(
            showInValidAmountPublishSubject
                .switchMap {
                    Observable.timer(1500, TimeUnit.MILLISECONDS)
                        .map<PartialState> { PartialState.HideAmountError }
                        .startWith(PartialState.ShowAmountError)
                },
            // set amount and amount calculation path when pressing digits
            intent<MiniCalculatorContract.Intent.OnDigitClicked>()
                .map {
                    val amountCalculationTemp: String = amountCalculation
                    val amountTemp: Long = amount

                    if (!isLastNumberExceededDecimal(amountCalculation)) {
                        amountCalculation = "$amountCalculation${it.digit}"
                        amount = MathUtils.eval(amountCalculation)
                    }

                    if (amount == -0L && amountCalculation != ".0") {
                        amount = 0L
                    }

                    if (amount.compareTo(0) == 0 && amountCalculation != ".0") {
                        amountCalculation = ""
                    }

                    if (amount < 0 || Math.round((amount / 100).toDouble()).toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
                        PartialState.SetAmountDetails(amount, amountCalculation)
                    }
                },
            // set amount and amount calculation path when pressing dot
            intent<Intent.OnDotClicked>()
                .map {
                    val amountCalculationTemp: String = amountCalculation
                    val amountTemp: Long = amount

                    if (amountCalculation.isEmpty()) {
                        amountCalculation = "$amountCalculation${"."}"
                    } else {
                        val lastNumber = findLastNumber(amountCalculation)
                        if (!lastNumber.contains('.')) {
                            amountCalculation = "$amountCalculation${"."}"
                        }
                    }

                    if (amount < 0 || Math.round((amount / 100).toDouble()).toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
                        PartialState.SetAmountDetails(amount, amountCalculation)
                    }
                },
            // set amount and amount calculation path when pressing digits
            intent<Intent.OnLongPressBackSpace>()
                .map {
                    amount = 0L
                    amountCalculation = ""
                    PartialState.SetAmountDetails(amount, amountCalculation)
                },
// set amount calculation when pressing equal
            intent<Intent.OnBackSpaceClicked>()
                .map {
                    val amountCalculationTemp: String = amountCalculation
                    val amountTemp: Long = amount
                    if (amountCalculation.isNotEmpty()) {
                        amountCalculation = amountCalculation.substring(0, amountCalculation.length - 1)
                        val lastChar: String =
                            if (amountCalculation.isNotEmpty()) amountCalculation.substring(amountCalculation.length - 1) else ""

                        if (amountCalculation.isEmpty()) {
                            amount = 0L
                        } else if (lastChar.isNotEmpty()) {
                            if (Character.isDigit(lastChar.toCharArray()[0])) {
                                amount = MathUtils.eval(amountCalculation)
                            } else {
                                val lLastIndexOfDigit = MathUtils.findLastIndexOfDigit(amountCalculation)
                                if (lLastIndexOfDigit != -1) {
                                    amount = MathUtils.eval(amountCalculation.substring(0, lLastIndexOfDigit))
                                }
                            }
                        }
                    }
                    if (amount.compareTo(0) == 0) {
                        amountCalculation = ""
                    }
                    if (amount < 0 || Math.round((amount / 100).toDouble()).toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
                        PartialState.SetAmountDetails(
                            amount,
                            amountCalculation
                        )
                    }
                }
        )
    }

    private fun isLastNumberExceededDecimal(amountCalculation: String): Boolean {
        val lastNumber = findLastNumber(amountCalculation)
        val lastIndexOfDot = lastNumber.lastIndexOf('.')
        return if (lastIndexOfDot >= 0) {
            lastNumber.substring(lastIndexOfDot + 1, lastNumber.length).length >= 2
        } else {
            false
        }
    }

    private fun findLastNumber(amountCalculation: String): String {
        val lastIndex = amountCalculation.lastIndexOfAny(charArrayOf('+', '-', '*', '/'))
        return if (lastIndex >= 0) {
            amountCalculation.substring(lastIndex + 1, amountCalculation.length)
        } else {
            amountCalculation
        }
    }

    override fun reduce(
        currentState: MiniCalculatorContract.State,
        partialState: PartialState
    ): MiniCalculatorContract.State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.ErrorState -> currentState.copy(
                isLoading = false,
                error = true
            )
            is PartialState.InitialData -> currentState
            is PartialState.SetAmountDetails -> currentState.copy(
                amountCalculation = partialState.amountCalculation,
                amount = partialState.amount
            )
            is PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is PartialState.NoChange -> currentState
            PartialState.ShowAmountError -> currentState
            PartialState.HideAmountError -> currentState
        }
    }

    override fun setNavigation(baseLayout: BaseLayout<MiniCalculatorContract.State>) {
        this.interactor = genericCastOrNull<MiniCalculatorContract.Interactor>(baseLayout)!!
    }
}
