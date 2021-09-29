package `in`.okcredit.shared.calculator

import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.BaseLayoutViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.calculator.CalculatorContract.Intent
import `in`.okcredit.shared.calculator.CalculatorContract.PartialState
import `in`.okcredit.shared.utils.MathUtils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

class CalculatorViewModel @Inject constructor(var initialState: CalculatorContract.State) :
    BaseLayoutViewModel<CalculatorContract.State, PartialState>(
        initialState,
        Schedulers.newThread(),
        Schedulers.newThread()
    ) {

    private var amount: Long = 0
    private var amountCalculation: String = ""
    private var calculatorOperatorsUsed = ""
    private lateinit var interactor: CalculatorContract.Interactor
    private val showInValidAmountPublishSubject: PublishSubject<Unit> = PublishSubject.create()

    override fun handle(): Observable<out UiState.Partial<CalculatorContract.State>> {
        return Observable.mergeArray(
            observeInitialDate(),
            showInValidAmountPublishSubject
                .switchMap {
                    Observable.timer(1500, TimeUnit.MILLISECONDS)
                        .map<PartialState> { PartialState.HideAmountError }
                        .startWith(PartialState.ShowAmountError)
                },
            // set amount and amount calculation path when pressing digits
            intent<Intent.OnDigitClicked>()
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

                    if (amount < 0 || (amount / 100).toDouble().roundToInt().toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
//                        amount = Math.round(amount * 100) / 100f
                        PartialState.SetAmountDetails(amount, amountCalculation, calculatorOperatorsUsed)
                    }
                },
            // set amount and amount calculation path when pressing operators
            intent<Intent.OnOperatorClicked>()
                .map {
                    val amountCalculationTemp: String = amountCalculation
                    val amountTemp: Long = amount
                    if (amountTemp > 0) {
                        addCalculatorOperatorsUsed(it.operator)
                    }

                    if (it.operator == "-") {
                        if (amountCalculation.isEmpty()) {
                            amountCalculation = "$amountCalculation${it.operator}"
                        } else if (amountCalculation.substring(amountCalculation.length - 1).isDidgitsOnly()) {
                            amountCalculation = "$amountCalculation${it.operator}"
                        } else if (amountCalculation.substring(amountCalculation.length - 1) == "+" ||
                            amountCalculation.substring(amountCalculation.length - 1) == "."
                        ) {
                            amountCalculation =
                                amountCalculation.substring(0, amountCalculation.length - 1) + it.operator
                        } else if (amountCalculation.substring(amountCalculation.length - 1) != "-") {
                            amountCalculation = "$amountCalculation${it.operator}"
                        }
                    } else {
                        if (amountCalculation.isEmpty()) {
                        } else if (amountCalculation.substring(amountCalculation.length - 1).isDidgitsOnly()) {
                            amountCalculation = "$amountCalculation${it.operator}"
                        } else if (amountCalculation.substring(amountCalculation.length - 1) == "." ||
                            amountCalculation.substring(amountCalculation.length - 1) == "+" ||
                            amountCalculation.substring(amountCalculation.length - 1) == "*"
                        ) {
                            amountCalculation =
                                amountCalculation.substring(0, amountCalculation.length - 1) + it.operator
                        } else if (amountCalculation.substring(amountCalculation.length - 1) == "-") {
                            if (amountCalculation.length > 2 &&
                                amountCalculation.substring(
                                        amountCalculation.length - 2,
                                        amountCalculation.length - 1
                                    ) == "*" ||
                                amountCalculation.substring(
                                        amountCalculation.length - 2,
                                        amountCalculation.length - 1
                                    ) == "+"
                            ) {
                                amountCalculation =
                                    amountCalculation.substring(0, amountCalculation.length - 2) + it.operator
                            } else {
                                amountCalculation =
                                    amountCalculation.substring(0, amountCalculation.length - 1) + it.operator
                            }
                        }
                    }

                    if (amount.compareTo(0) == 0) {
                        amountCalculation = ""
                    }

                    if (amount < 0 || (amount / 100).toDouble().roundToInt().toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
                        PartialState.SetAmountDetails(amount, amountCalculation, calculatorOperatorsUsed)
                    }
                },
            // set amount and amount calculation path when pressing dot
            intent<Intent.OnDotClicked>()
                .map {
                    addCalculatorOperatorsUsed(".")
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

                    if (amount < 0 || (amount / 100).toDouble().roundToInt().toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
                        PartialState.SetAmountDetails(amount, amountCalculation, calculatorOperatorsUsed)
                    }
                },
            // set amount calculation when pressing equal
            intent<Intent.OnEqualClicked>()
                .map {
                    addCalculatorOperatorsUsed("=")
                    val amountCalculationTemp: String = amountCalculation
                    val amountTemp: Long = amount

                    amountCalculation = if (amount % 100 != 0L) {
                        (amount / 100).toString() + "." + (amount % 100).toString()
                    } else {
                        (amount / 100).toString()
                    }

                    if (amount.compareTo(0) == 0) {
                        amountCalculation = ""
                    }

                    if (amount < 0 || (amount / 100).toDouble().roundToInt().toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
                        PartialState.SetAmountDetails(amount, amountCalculation, calculatorOperatorsUsed)
                    }
                },
            showInValidAmountPublishSubject
                .switchMap {
                    Observable.timer(1500, TimeUnit.MILLISECONDS)
                        .map<PartialState> { PartialState.HideAmountError }
                        .startWith(PartialState.ShowAmountError)
                },
            // set amount and amount calculation path when pressing digits
            intent<Intent.ClearAmount>()
                .map {
                    amount = 0L
                    amountCalculation = ""
                    PartialState.SetAmountDetails(amount, amountCalculation, calculatorOperatorsUsed)
                },
            // set amount and amount calculation path when pressing digits
            intent<Intent.OnLongPressBackSpace>()
                .map {
                    addCalculatorOperatorsUsed("D")
                    amount = 0L
                    amountCalculation = ""
                    PartialState.SetAmountDetails(amount, amountCalculation, calculatorOperatorsUsed)
                },
            // set amount calculation when pressing equal
            intent<Intent.OnBackSpaceClicked>()
                .map {
                    addCalculatorOperatorsUsed("D")
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
                    if (amount < 0 || (amount / 100).toDouble().roundToInt().toString().length > 7) {
                        amountCalculation = amountCalculationTemp
                        amount = amountTemp
                        showInValidAmountPublishSubject.onNext(Unit)
                        PartialState.NoChange
                    } else {
                        PartialState.SetAmountDetails(
                            amount,
                            amountCalculation,
                            calculatorOperatorsUsed
                        )
                    }
                }
        )
    }

    private fun observeInitialDate() = intent<Intent.LoadInitialData>().map {
        if (amount == 0L && amountCalculation == "") {
            amount = it.initialData.initialAmount
            amountCalculation = it.initialData.initialAmountCalculation
        }
        PartialState.NoChange
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

    private fun addCalculatorOperatorsUsed(value: String) {
        calculatorOperatorsUsed += value
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
        currentState: CalculatorContract.State,
        partialState: PartialState,
    ): CalculatorContract.State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.ErrorState -> currentState.copy(
                isLoading = false,
                error = true
            )
            is PartialState.SetAmountDetails -> currentState.copy(
                isLoading = false,
                amountCalculation = partialState.amountCalculation,
                amount = partialState.amount,
                amountError = false,
                calculatorOperatorsUsed = partialState.calculatorOperatorsUsed
            )
            is PartialState.ShowAmountError -> currentState.copy(isLoading = false, amountError = true)
            is PartialState.HideAmountError -> currentState.copy(amountError = false)
            PartialState.InvalidAmountError -> currentState.copy(invalidAmountError = true)
        }
    }

    override fun setNavigation(baseLayout: BaseLayout<CalculatorContract.State>) {
        this.interactor = genericCastOrNull<CalculatorContract.Interactor>(baseLayout)!!
    }

    private fun CharSequence.isDidgitsOnly(): Boolean {
        return toString().matches("[0-9]+".toRegex())
    }
}
