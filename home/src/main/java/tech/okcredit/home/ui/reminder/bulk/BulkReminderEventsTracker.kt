package tech.okcredit.home.ui.reminder.bulk

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey
import dagger.Lazy
import javax.inject.Inject

class BulkReminderEventsTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    companion object {
        const val BULK_REMINDER_DISMISSED = "Bulk Reminder Dismissed"
        const val BULK_REMINDER_CANCELLED = "Bulk Reminder Cancelled"
        const val SELECT_CUSTOMER = "Select Customer"
        const val SEND_BULK_REMINDER = "Send Bulk Reminder"
        const val BULK_REMINDER_SUCCESS = "Bulk Reminder Success"

        const val RELATION_CUSTOMER = "Customer"
        const val MERCHANT_ID = "merchant_id"
        const val ACCOUNT_ID = "account_id"
        const val SELECTED = "if_selected"
        const val DUE_AMOUNT = "due_amount"
        const val COLLECTION_ADOPTED = "collection_adopted"
        const val NUMBER_OF_ACCOUNTS = "number_of_accounts_bulk_reminder_sent"
    }

    fun trackBulkReminderDismissed(
        merchant_id: String,
    ) {
        val eventProperties = mapOf<String, Any>(
            MERCHANT_ID to merchant_id,
        )
        analyticsProvider.get().trackEvents(BULK_REMINDER_DISMISSED, eventProperties)
    }

    fun trackBulkReminderCancelled(
        merchant_id: String,
    ) {
        val eventProperties = mapOf<String, Any>(
            MERCHANT_ID to merchant_id,
        )
        analyticsProvider.get().trackEvents(BULK_REMINDER_CANCELLED, eventProperties)
    }

    fun trackSelectCustomer(
        merchant_id: String,
        accountId: String,
        dueAmount: Long,
        selected: Boolean
    ) {
        val eventProperties = mapOf<String, Any>(
            MERCHANT_ID to merchant_id,
            PropertyKey.RELATION to RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
            DUE_AMOUNT to dueAmount,
            SELECTED to selected
        )
        analyticsProvider.get().trackEvents(SELECT_CUSTOMER, eventProperties)
    }

    fun trackSendBulkReminder(
        merchant_id: String,
        accountIds: String,
        collectionAdopted: Boolean,
        numberOfAccountsSelected: Int
    ) {
        val eventProperties = mapOf<String, Any>(
            MERCHANT_ID to merchant_id,
            PropertyKey.ACCOUNT_ID to accountIds,
            COLLECTION_ADOPTED to collectionAdopted,
            NUMBER_OF_ACCOUNTS to numberOfAccountsSelected
        )
        analyticsProvider.get().trackEvents(SEND_BULK_REMINDER, eventProperties)
    }

    fun trackBulkReminderSuccess(
        merchant_id: String,
    ) {
        val eventProperties = mapOf<String, Any>(
            MERCHANT_ID to merchant_id,
        )
        analyticsProvider.get().trackEvents(BULK_REMINDER_SUCCESS, eventProperties)
    }
}
