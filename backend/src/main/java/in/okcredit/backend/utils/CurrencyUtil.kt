package `in`.okcredit.backend.utils

import `in`.okcredit.backend.R
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import merchant.okcredit.accounting.model.Transaction
import tech.okcredit.android.base.language.LocaleManager.Companion.englishLocale
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object CurrencyUtil {

    private val symbols by lazy { DecimalFormatSymbols(englishLocale) }

    @JvmStatic
    fun currencyDisplayFormat(amount: Double): String {
        return format("##,##,###.##", amount)
    }

    @JvmStatic
    fun renderV2(amount: Long, textView: TextView, txType: Int) {
        @ColorRes var color = R.color.tx_payment
        if (amount < 0L) {
            color = R.color.red_primary
        } else if (txType == Transaction.CREDIT) {
            color = R.color.red_primary
        }
        textView.text = String.format("₹%s", formatV2(amount))
        textView.setTextColor(ContextCompat.getColor(textView.context, color))
    }

    @JvmStatic
    fun renderV2(amount: Long, textView: TextView, isPayment: Boolean?) {
        @ColorRes var color = R.color.tx_payment
        if (amount < 0L) {
            color = R.color.red_primary
        } else if (!isPayment!!) {
            color = R.color.red_primary
        }
        textView.text = String.format("₹%s", formatV2(amount))
        textView.setTextColor(ContextCompat.getColor(textView.context, color))
    }

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

    @JvmStatic
    fun renderAsSubtitle(textView: TextView?, amount: Long) {
        if (textView != null) {
            textView.text = String.format("₹%s", formatV2(amount))
        }
    }

    @JvmStatic
    fun renderArrows(amount: Long, arrows: ImageView, txType: Int) {
        var drawable = ContextCompat.getDrawable(arrows.context, R.drawable.ic_payment_down_arrow)
        arrows.rotation = 180f
        if (amount < 0L) {
            drawable = ContextCompat.getDrawable(arrows.context, R.drawable.ic_credit_up)
            arrows.rotation = 180f
        } else if (txType == Transaction.CREDIT) {
            drawable = ContextCompat.getDrawable(arrows.context, R.drawable.ic_credit_up)
            arrows.rotation = 180f
        }
        arrows.setImageDrawable(drawable)
    }

    @JvmStatic
    fun renderArrowsV2(amount: Long, arrows: ImageView, isPayment: Boolean?) {
        var drawable = ContextCompat.getDrawable(arrows.context, R.drawable.ic_give)
        if (amount < 0L) {
            drawable = ContextCompat.getDrawable(arrows.context, R.drawable.ic_take)
        } else if (!isPayment!!) {
            drawable = ContextCompat.getDrawable(arrows.context, R.drawable.ic_take)
        }
        arrows.setImageDrawable(drawable)
    }

    @JvmStatic
    fun renderArrowsV3(amount: Long, arrows: ImageView, isPayment: Boolean?) {
        var drawable = ContextCompat.getDrawable(arrows.context, R.drawable.ic_payment_down_arrow)
        if (amount < 0L) {
            drawable = ContextCompat.getDrawable(arrows.context, R.drawable.ic_credit_up)
        } else if (!isPayment!!) {
            drawable = ContextCompat.getDrawable(arrows.context, R.drawable.ic_credit_up)
        }
        arrows.setImageDrawable(drawable)
    }

    @JvmStatic
    fun renderDeletedAmount(amount: Long, textView: TextView) {
        textView.text = String.format("₹%s", formatV2(amount))
    }

    @JvmStatic
    fun renderCustomerCredit(amount: Long, textView: TextView) {
        @ColorRes val color = R.color.red_primary
        textView.text = String.format("₹%s", formatV2(amount))
        textView.setTextColor(ContextCompat.getColor(textView.context, color))
    }

    @JvmStatic
    fun renderAmount(amount: Long, textView: TextView) {
        @ColorRes val color = R.color.black
        textView.text = String.format("₹%s", formatV2(amount))
        textView.setTextColor(ContextCompat.getColor(textView.context, color))
    }
}
