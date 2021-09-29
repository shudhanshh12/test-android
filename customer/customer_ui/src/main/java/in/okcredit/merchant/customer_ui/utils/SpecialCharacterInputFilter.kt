package `in`.okcredit.merchant.customer_ui.utils

import android.text.InputFilter
import android.text.Spanned

class SpecialCharacterInputFilter : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        source?.forEach {
            if (!Character.isLetterOrDigit(it)) {
                if (it != ' ' && it != '-')
                    return ""
            }
        }

        return null
    }
}
