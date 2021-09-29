package `in`.okcredit.supplier.utils

import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.supplier.R
import android.content.Context
import org.joda.time.format.DateTimeFormat
import tech.okcredit.android.base.language.LocaleManager

fun Context.getPaidDayText(supplier: Supplier): String {
    val dateFormatter = DateTimeFormat.forPattern("dd MMM, YYYY").withLocale(LocaleManager.englishLocale)
    var paidDay =
        getString(R.string.supplier_added_on_date, supplier.createTime.toString(dateFormatter))
    if (supplier.lastActivityTime != null) {
        val duration = org.joda.time.Duration(
            supplier.lastActivityTime,
            org.joda.time.DateTime().withTimeAtStartOfDay().plusDays(1)
        )
        paidDay = getString(
            R.string.supplier_last_payment,
            if (duration.standardDays == 0L)
                getString(R.string.paid_today)
            else if (duration.standardDays == 1L)
                getString(R.string.paid_yesterday)
            else if (duration.standardDays > 30 && supplier.balance < 0) {
                if (supplier.lastActivityTime != null) supplier.lastActivityTime!!.toString(dateFormatter) else "-"
            } else if (supplier.lastActivityTime != null) supplier.lastActivityTime!!.toString(
                dateFormatter
            ) else "-"
        )
    }

    return paidDay
}
