package `in`.okcredit.merchant.customer_ui.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import tech.okcredit.android.base.language.LocaleManager.Companion.englishLocale
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class CurrencyTextWatcher constructor(
    private val currencyInputLayout: EditText,
    private val amountListener: (amount: Double) -> Unit
) : TextWatcher {

    private var ignoreIteration = false
    private var lastGoodInput: String? = null
    private val currencyFormatter by lazy {
        val symbols = DecimalFormatSymbols.getInstance(englishLocale)
        DecimalFormat("##,##,##,###.##", symbols).apply {
            maximumFractionDigits = 2
        }
    }

    /**
     * A specialized TextWatcher designed specifically for converting EditText values to a pretty-print string currency value.
     * @param textBox The EditText box to which this TextWatcher is being applied.
     * Used for replacing user-entered text with formatted text as well as handling cursor position for inputting monetary values
     */
    init {
        lastGoodInput = ""
        ignoreIteration = false
    }

    /**
     * After each letter is typed, this method will take in the current text, process it, and take the resulting
     * formatted string and place it back in the EditText box the TextWatcher is applied to
     * @param editable text to be transformed
     */
    override fun afterTextChanged(editable: Editable) {
        // Use the ignoreIteration flag to stop our edits to the text field from triggering an endlessly recursive call to afterTextChanged
        if (!ignoreIteration) {
            ignoreIteration = true
            // Start by converting the editable to something easier to work with, then remove all non-digit characters
            var newText = editable.toString()
            val textToDisplay: String?
            newText = newText.replace("[^0-9.]".toRegex(), "")
            when {
                newText.isEmpty() -> {
                    lastGoodInput = ""
                    amountListener.invoke(0.0)
                    currencyInputLayout.setText("")
                    return
                }
                newText.endsWith(".") -> {
                    val tempText = newText.replace(".", "")
                    amountListener.invoke(tempText.toDoubleOrNull() ?: 0.0)
                    ignoreIteration = false
                    return
                }
                newText.endsWith(".0") -> {
                    amountListener.invoke(newText.toDoubleOrNull() ?: 0.0)
                    ignoreIteration = false
                    return
                }
                newText != "" && newText != "-" -> {
                    amountListener.invoke(newText.toDoubleOrNull() ?: 0.0)
                }
            }

            textToDisplay = try {
                formatText(newText)
            } catch (exception: IllegalArgumentException) {
                lastGoodInput
            }
            currencyInputLayout.setText(textToDisplay)
            // Store the last known good input so if there are any issues with new input later, we can fall back gracefully.
            lastGoodInput = textToDisplay
            // locate the position to move the cursor to, which will always be the last digit.
            val currentText: String = currencyInputLayout.text.toString()
            val cursorPosition = indexOfLastDigit(currentText) + 1
            // Move the cursor to the end of the numerical value to enter the next number in a right-to-left fashion,
            // like you would on a calculator.
            if (currentText.length >= cursorPosition) {
                currencyInputLayout.setSelection(cursorPosition)
            }
        } else {
            ignoreIteration = false
        }
    }

    // Thanks to Lucas Eduardo for this contribution to update the cursor placement code.
    private fun indexOfLastDigit(str: String): Int {
        var result = 0
        for (i in str.indices) {
            if (Character.isDigit(str[i])) {
                result = i
            }
        }
        return result
    }

    /**
     * Formats the digits to required DecimalFormat
     */
    private fun formatText(value: String): String { // special case for the start of a negative number
        var tempValue = value

        // strip all non-digits so the formatter always has a 'clean slate' of numbers to work with
        tempValue = tempValue.replace("[^0-9.]".toRegex(), "")
        // if there's nothing left, that means we were handed an empty string. Also, cap the raw input so the formatter
        // doesn't break.
        if (value != "") { // if we're given a value that's smaller than our decimal location, pad the value.
            // place the decimal in the proper location to construct a double which we will give the formatter.
            // This is NOT the decimal separator for the currency value, but for the double which drives it.
            val preparedVal = StringBuilder(tempValue).toString()
            // Convert the string into a double, which will be passed into the currency formatter
            val newTextValue = preparedVal.toDouble()
            // finally, do the actual formatting
            tempValue = currencyFormatter.format(newTextValue)
        } else {
            throw IllegalArgumentException("Invalid amount of digits found (either zero or too many) in argument value")
        }
        return tempValue
    }

    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
}
