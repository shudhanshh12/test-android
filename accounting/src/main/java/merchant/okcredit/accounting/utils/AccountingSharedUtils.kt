package merchant.okcredit.accounting.utils

import android.content.Context
import merchant.okcredit.accounting.R
import merchant.okcredit.accounting.contract.model.LedgerType
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils
import java.util.concurrent.TimeUnit

object AccountingSharedUtils {

    enum class TxnGravity {
        LEFT, RIGHT
    }

    fun findUiTxnGravity(isPayment: Boolean, ledgerType: LedgerType): TxnGravity {
        return if (isPayment) {
            if (ledgerType == LedgerType.CUSTOMER) TxnGravity.LEFT else TxnGravity.RIGHT
        } else {
            if (ledgerType == LedgerType.CUSTOMER) TxnGravity.RIGHT else TxnGravity.LEFT
        }
    }

    fun findFormattedDateOrTime(createdAt: DateTime, billDate: DateTime, showTimeOnly: Boolean = false): String {
        return if (billDate.withTimeAtStartOfDay() == createdAt.withTimeAtStartOfDay() || showTimeOnly) {
            DateTimeUtils.formatTimeOnly(billDate)
        } else {
            DateTimeUtils.formatDateOnly(billDate)
        }
    }

    fun ellipsizeName(name: String?): String {
        val maxLength = 10
        return when {
            name.isNullOrBlank() -> ""
            name.length > maxLength -> name.substring(0, maxLength) + "..."
            else -> name
        }
    }

    fun isSevenDaysPassed(billDate: DateTime) =
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis().minus(billDate.millis)) > 7

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
}
