package `in`.okcredit.payment.utils

import tech.okcredit.android.base.language.LocaleManager.Companion.englishLocale
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object CurrencyUtil {

    private val symbols by lazy { DecimalFormatSymbols(englishLocale) }

    @JvmStatic
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
}
