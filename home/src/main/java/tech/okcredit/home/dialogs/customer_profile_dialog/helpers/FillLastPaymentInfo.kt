package tech.okcredit.home.dialogs.customer_profile_dialog.helpers

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.home.R

fun TextView.setLastPaymentInfo(customer: Customer) {
    val DATE_FORMATTER = DateTimeFormat.forPattern("dd MMM, YYYY").withLocale(LocaleManager.englishLocale)

    if (customer.lastPayment != null && customer.lastPayment!!.isAfter(customer.createdAt)) {
        val duration = Duration(customer.lastPayment, DateTime().withTimeAtStartOfDay().plusDays(1))
        if (duration.standardDays == 0L)
            text =
                "(" + context.getString(R.string.last_payment) + ": " + context.getString(R.string.paid_today) + ")"
        else if (duration.standardDays == 1L)
            text =
                "(" + context.getString(R.string.last_payment) + ": " + context.getString(R.string.paid_yesterday) + ")"
        else if (duration.standardDays > 30 && customer.balanceV2 < 0) {
            text =
                "(" + context.getString(R.string.last_payment) + ": " + if (customer.lastPayment != null) customer.lastPayment!!.toString(
                DATE_FORMATTER
            ) + ")" else "-"
        } else
            text =
                "(" + context.getString(R.string.last_payment) + ": " + if (customer.lastPayment != null) customer.lastPayment!!.toString(
                DATE_FORMATTER
            ) + ")" else "-"
    } else {
        text = "(" + context.getString(R.string.added_on_date, customer.createdAt?.toString(DATE_FORMATTER)) + ")"
    }
}

fun TextView.setAmountInfo(customer: Customer) {
    @ColorRes var color = R.color.tx_payment
    var dueLabel = context.getString(R.string.advance)
    if (customer.balanceV2 < 0L) {
        color = R.color.red_primary
        dueLabel = context.getString(R.string.due)
    }
    text = String.format("%s ₹%s", dueLabel, CurrencyUtil.formatV2(customer.balanceV2))
    setTextColor(ContextCompat.getColor(context, color))
}
