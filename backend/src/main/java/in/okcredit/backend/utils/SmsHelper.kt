package `in`.okcredit.backend.utils

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.backend.analytics.AnalyticsSuperProps
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.resources.R
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import dagger.Lazy
import merchant.okcredit.accounting.model.Transaction
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

class SmsHelper @Inject constructor(
    private val context: Lazy<Context>,
    private val localeManager: Lazy<LocaleManager>,
) {

    fun getTransactionSmsText(
        customer: Customer,
        business: Business,
        transaction: Transaction,
    ): String {
        val amountValue = (transaction.amountV2.toDouble() / 100).toString()

        return if (transaction.type == Transaction.CREDIT) {
            context.get().getString(
                R.string.reminder_credit,
                amountValue,
                business.name,
                getBalance(customer.balanceV2),
                customer.accountUrl
            )
        } else {
            context.get().getString(
                R.string.reminder_payment,
                amountValue,
                business.name,
                getBalance(customer.balanceV2),
                customer.accountUrl
            )
        }
    }

    private fun getBalance(balance: Long): String {
        return if (balance <= 0) {
            CurrencyUtil.formatV2(balance) + " " + context.get().getString(R.string.due)
        } else {
            CurrencyUtil.formatV2(balance) + " " + context.get().getString(R.string.advance)
        }
    }

    fun openSmsAppForReminders(smsText: String?, mobile: String?) {
        Analytics.setUserProperty(AnalyticsSuperProps.REMINDER_SMS, null)

        val defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context.get())

        val sendIntent = Intent(Intent.ACTION_SENDTO)
        sendIntent.data = Uri.parse("smsto:" + mobile!!)
        sendIntent.putExtra("sms_body", smsText)

        if (defaultSmsPackageName != null) {
            sendIntent.setPackage(defaultSmsPackageName)
        }
        context.get().startActivity(sendIntent)
    }

    fun getReminderSmsText(customer: Customer, business: Business): String {
        return if (localeManager.get().getLanguage() == LocaleManager.LANGUAGE_HINDI) {
            String.format(
                "%s %s \n%s %s. %s %s",
                business.name,
                context.get().getString(R.string.payment_reminder_by),
                context.get().getString(R.string.payment_reminder_amount),
                CurrencyUtil.formatV2(customer.balanceV2),
                context.get().getString(R.string.payment_reminder_check_details),
                customer.accountUrl
            )
        } else {
            String.format(
                "%s %s. \n%s %s. %s %s",
                context.get().getString(R.string.payment_reminder_by),
                business.name,
                context.get().getString(R.string.payment_reminder_amount),
                CurrencyUtil.formatV2(customer.balanceV2),
                context.get().getString(R.string.payment_reminder_check_details),
                customer.accountUrl
            )
        }
    }
}
