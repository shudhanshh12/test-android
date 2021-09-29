package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.sales_ui.utils.SalesUtil
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class Calculator @Inject constructor() : UseCase<Calculator.Request, Calculator.Response> {

    data class Request(val mode: Mode, val value: String, val amountCalculation: String, val amount: Long)

    data class Response(val amountCalculation: String, val amount: Long)

    enum class Mode {
        Operator, Digit, Dot, Backspace, Equals
    }
    override fun execute(req: Request): Observable<Result<Response>> {
        return UseCase.wrapObservable(
            when (req.mode) {
                Mode.Operator -> onOperatorClicked(req.value, req.amountCalculation, req.amount)
                Mode.Digit -> onDigitClicked(req.value, req.amountCalculation, req.amount)
                Mode.Dot -> onDotClicked(req.amountCalculation, req.amount)
                Mode.Backspace -> onBackSpaceClicked(req.amountCalculation, req.amount)
                Mode.Equals -> onEqualsClicked(req.amount)
            }
        )
    }

    private fun onBackSpaceClicked(amountCalculation: String, amount: Long): Observable<Response> {
        var resultAmount = amount
        var resultAmountCalculation = amountCalculation

        if (resultAmountCalculation.isNotEmpty()) {
            resultAmountCalculation = resultAmountCalculation.substring(0, resultAmountCalculation.length - 1)
            val lastChar: String = if (resultAmountCalculation.isNotEmpty()) resultAmountCalculation.substring(resultAmountCalculation.length - 1) else ""

            if (resultAmountCalculation.isEmpty()) {
                resultAmount = 0L
            } else if (lastChar.isNotEmpty()) {
                if (Character.isDigit(lastChar.toCharArray()[0])) {
                    resultAmount = SalesUtil.eval(resultAmountCalculation)
                } else {
                    val lLastIndexOfDigit = SalesUtil.findLastIndexOfDigit(resultAmountCalculation)
                    if (lLastIndexOfDigit != -1) {
                        resultAmount = SalesUtil.eval(resultAmountCalculation.substring(0, lLastIndexOfDigit))
                    }
                }
            }
        }

        if (resultAmount < 0 || Math.round((resultAmount / 100).toDouble()).toString().length > 7) {
            return Observable.just(Response(amountCalculation, amount))
        }
        return Observable.just(Response(resultAmountCalculation, resultAmount))
    }

    private fun onEqualsClicked(amount: Long): Observable<Response> {
        var resultAmountCalculation: String

        resultAmountCalculation = if (amount % 100 != 0L) {
            (amount / 100).toString() + "." + (amount % 100).toString()
        } else {
            (amount / 100).toString()
        }

        if (amount.compareTo(0) == 0) {
            resultAmountCalculation = ""
        }
        return Observable.just(Response(resultAmountCalculation, amount))
    }

    private fun onDotClicked(amountCalculation: String, amount: Long): Observable<Response> {
        var resultAmountCalculation = amountCalculation

        if (resultAmountCalculation.isEmpty()) {
            resultAmountCalculation = "$resultAmountCalculation${"."}"
        } else {
            val lastNumber = findLastNumber(resultAmountCalculation)
            if (!lastNumber.contains('.')) {
                resultAmountCalculation = "$resultAmountCalculation${"."}"
            }
        }
        return Observable.just(Response(resultAmountCalculation, amount))
    }

    private fun onOperatorClicked(operator: String, amountCalculation: String, amount: Long): Observable<Response> {
        var resultAmountCalculation = amountCalculation

        if (operator == "-") {
            if (resultAmountCalculation.isEmpty()) {
                resultAmountCalculation = "$resultAmountCalculation$operator"
            } else if (resultAmountCalculation.substring(resultAmountCalculation.length - 1).isDidgitsOnly()) {
                resultAmountCalculation = "$resultAmountCalculation$operator"
            } else if (resultAmountCalculation.substring(resultAmountCalculation.length - 1) == "+" ||
                resultAmountCalculation.substring(resultAmountCalculation.length - 1) == "."
            ) {
                resultAmountCalculation = resultAmountCalculation.substring(0, resultAmountCalculation.length - 1) + operator
            } else if (resultAmountCalculation.substring(resultAmountCalculation.length - 1) != "-") {
                resultAmountCalculation = "$resultAmountCalculation$operator"
            }
        } else {
            if (resultAmountCalculation.isEmpty()) {
                // Do nothing
            } else if (resultAmountCalculation.substring(resultAmountCalculation.length - 1).isDidgitsOnly()) {
                resultAmountCalculation = "$resultAmountCalculation$operator"
            } else if (resultAmountCalculation.substring(resultAmountCalculation.length - 1) == "." ||
                resultAmountCalculation.substring(resultAmountCalculation.length - 1) == "+" ||
                resultAmountCalculation.substring(resultAmountCalculation.length - 1) == "*"
            ) {
                resultAmountCalculation = resultAmountCalculation.substring(0, resultAmountCalculation.length - 1) + operator
            } else if (resultAmountCalculation.substring(resultAmountCalculation.length - 1) == "-") {
                if (resultAmountCalculation.length > 2 &&
                    resultAmountCalculation.substring(resultAmountCalculation.length - 2, resultAmountCalculation.length - 1) == "*" ||
                    resultAmountCalculation.substring(resultAmountCalculation.length - 2, resultAmountCalculation.length - 1) == "+"
                ) {
                    resultAmountCalculation = resultAmountCalculation.substring(0, resultAmountCalculation.length - 2) + operator
                } else {
                    resultAmountCalculation = resultAmountCalculation.substring(0, resultAmountCalculation.length - 1) + operator
                }
            }
        }

        return Observable.just(Response(resultAmountCalculation, amount))
    }

    private fun onDigitClicked(digit: String, amountCalculation: String, amount: Long): Observable<Response> {
        var resultAmount = amount
        var resultAmountCalculation = amountCalculation
        if (!isLastNumberExceededDecimal(resultAmountCalculation)) {
            resultAmountCalculation = "$resultAmountCalculation$digit"
            resultAmount = SalesUtil.eval(resultAmountCalculation)
        }

        if (resultAmount == -0L && resultAmountCalculation != ".0") {
            resultAmount = 0L
        }

        if (resultAmount < 0 || Math.round((resultAmount / 100).toDouble()).toString().length > 7) {
            return Observable.just(Response(amountCalculation, amount))
        }
        return Observable.just(Response(resultAmountCalculation, resultAmount))
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

    private fun CharSequence.isDidgitsOnly(): Boolean {
        return toString().matches("[0-9]+".toRegex())
    }
}
