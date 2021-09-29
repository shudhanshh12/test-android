package `in`.okcredit.voice_first.utils

import `in`.okcredit.voice_first.R
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import tech.okcredit.android.base.language.LocaleManager.Companion.englishLocale
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.ceil

object CurrencyUtil {

    private val symbols by lazy { DecimalFormatSymbols(englishLocale) }
    internal val numRegex by lazy { Regex("""[0-9]{0,7}(\.[0-9]{0,2})?""") }

    fun formatV2(amount: Long): String {
        var amountTemp = amount
        if (amountTemp < 0L) {
            amountTemp *= -1
        }
        val fraction = amountTemp % 100
        val fractionString: String = when {
            fraction == 0L -> {
                ""
            }
            fraction < 10 -> {
                ".0$fraction"
            }
            else -> {
                ".$fraction"
            }
        }
        amountTemp /= 100
        return if (amountTemp < 1000) {
            String.format("%s%s", format("###", amountTemp), fractionString)
        } else {
            val hundreds = (amountTemp % 1000).toDouble()
            val other = (amountTemp / 1000).toInt()
            String.format(
                "%s,%s%s",
                format(",##", other),
                format("000", hundreds),
                fractionString
            )
        }
    }

    private fun format(pattern: String, value: Any): String {
        return DecimalFormat(pattern, symbols).format(value)
    }

    fun ImageView.renderArrowsForSupplier(amount: Long, isPayment: Boolean) {
        val drawableRes = if (amount < 0 || !isPayment) R.drawable.ic_take else R.drawable.ic_give
        setImageDrawable(ContextCompat.getDrawable(context, drawableRes))
    }

    fun ImageView.renderArrowsForCustomer(amount: Long, isPayment: Boolean) {
        val drawableRes = if (amount < 0 || !isPayment) R.drawable.ic_credit_up else R.drawable.ic_payment_down_arrow
        setImageDrawable(ContextCompat.getDrawable(context, drawableRes))
    }

    fun TextView.renderAmountColored(amount: Long, isPayment: Boolean) {
        val color = if (amount < 0 || !isPayment) R.color.red_primary else R.color.tx_payment
        text = String.format("₹%s", formatV2(amount))
        setTextColor(ContextCompat.getColor(context, color))
    }

    fun TextView.renderAmount(amount: Long) {
        val color = R.color.grey900
        text = String.format("₹%s", formatV2(amount))
        setTextColor(ContextCompat.getColor(context, color))
    }

    fun EditText.acceptOnlyCurrency() {
        addTextChangedListener(object : TextWatcher {
            private var lastAmount = 0L

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val newAmountStr: String? = s?.toString()
                    ?.filter { it.isDigit() || it == '.' }
                    ?.takeIf { it.isNotBlank() }

                // Empty input, use 0Long
                if (newAmountStr == null) {
                    renderAmount(0)
                    setSelection(text.toString().length)
                    return
                }

                // Invalid input. overwrite with previous valid input
                if (!newAmountStr.matches(numRegex)) {
                    renderAmount(lastAmount)
                    setSelection(text.toString().length)
                    return
                }

                val newAmount = ceil(newAmountStr.toDouble() * 100).toLong()
                if (newAmount != lastAmount) {
                    lastAmount = newAmount
                    renderAmount(newAmount)
                    setSelection(text.toString().length)
                    return
                }
            }
        })
    }
}
