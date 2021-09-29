package tech.okcredit.android.base.utils

import android.text.InputFilter
import android.text.Spanned

/**
 * Input filter which ensures only one decimal is entered in the input. Also we can maintain max digits
 * before decimal and after decimal using this filter.
 */
class InputFilterDecimal(
    private val beforeDecimal: Int,
    private val afterDecimal: Int,
    private val invalidDigitEntered: (Boolean) -> Unit
) : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val builder = StringBuilder(dest)
        builder.insert(dstart, source)
        val temp = builder.toString()
        // / check if entered char is `.` and it is already present in the previously entered text
        if (source == "." && dest.indexOf(".") != -1) {
            return ""
        }
        // if the final text is just `.` then return the formatted decimal
        if (temp == ".") {
            return "0."
        } else if (temp.indexOf('.') == -1) {
            // confirm the length of digits if decimal point is not present
            if (temp.length > beforeDecimal) {
                invalidDigitEntered.invoke(true)
                return ""
            }
        } else {
            if (".".equals(temp.substring(temp.indexOf('.') + 1), ignoreCase = true)) {
                return ""
            }
            if (temp.substring(0, temp.indexOf('.')).length > beforeDecimal) {
                invalidDigitEntered.invoke(true)
                return ""
            }

            if (temp.substring(temp.indexOf('.') + 1).length > afterDecimal) {
                invalidDigitEntered.invoke(false)
                return ""
            }
        }
        return null
    }
}
