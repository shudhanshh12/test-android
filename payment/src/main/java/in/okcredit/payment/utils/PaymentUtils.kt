package `in`.okcredit.payment.utils

import `in`.okcredit.payment.R
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

object PaymentUtils {
    fun String.isValidIfsc(): Boolean {
        val regExp: String = when {
            isEmpty() -> {
                return false
            }
            length <= 4 -> {
                "^[A-Z]{$length}$"
            }
            length == 5 -> {
                "^[A-Z]{4}[0]$"
            }
            else -> {
                "^[A-Z]{4}[0][A-Z0-9]{${length - 5}}$"
            }
        }

        return matches(regExp.toRegex())
    }

    fun isInvalidBankDetails(accountNumber: String, ifsc: String): Boolean {
        return (accountNumber.length < 9 || ifsc.isValidIfsc().not() || ifsc.length != 11)
    }
}

fun Long.formatDecimalString(): String {
    val fraction: Long = this % 100
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
    val digit = this / 100
    return String.format("%s%s", digit, fractionString)
}

fun Long.getDateTimeString(): String {
    return SimpleDateFormat("hh:mm a 'on' dd MMM yyyy").format(Date(this))
}

fun getFinalDueAmount(dueBalance: Long, remainingDailyLimit: Long): Long {
    return if (dueBalance < 0) {
        val absBalance = dueBalance.absoluteValue
        if (absBalance > remainingDailyLimit) remainingDailyLimit else absBalance
    } else 0
}

fun getWhatsAppMsg(
    context: Context,
    amount: String,
    paymentTime: String,
    txnId: String,
    status: String,
): String {
    return context.run {
        val helpString = getString(R.string.t_002_i_need_help)
        val amountString = getString(R.string.t_002_amount)
        val timeString = getString(R.string.t_002_payment_time)
        val txnString = getString(R.string.t_002_txn_id)
        val statusString = getString(R.string.t_002_status)
        getString(R.string.whatsapp_mono_space_template, helpString) +
            "\n${getString(R.string.whatsapp_mono_space_template, amountString)} : " +
            getString(R.string.whatsapp_bold_template, amount) +
            "\n${
            getString(R.string.whatsapp_mono_space_template, timeString)
            } : ${getString(R.string.whatsapp_bold_template, paymentTime)}" +
            "\n${
            getString(R.string.whatsapp_mono_space_template, txnString)
            } : ${getString(R.string.whatsapp_bold_template, txnId)}" +
            "\n${
            getString(R.string.whatsapp_mono_space_template, statusString)
            } : ${getString(R.string.whatsapp_bold_template, status)}"
    }
}
