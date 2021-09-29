package `in`.okcredit.merchant.customer_ui.ui.subscription

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import dagger.Lazy
import javax.inject.Inject

class SubscriptionEventTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    companion object {
        private const val EVENT_SUBSCRIPTION_CLICK = "subscription_click"
        private const val EVENT_SUBSCRIPTION_TRANSACTION_STARTED = "subscription_transaction_started"
        private const val EVENT_SUBSCRIPTION_AMOUNT_ENTERED = "subscription_amount_entered"
        private const val EVENT_SUBSCRIPTION_NAME_STARTED = "subscription_name_started"
        private const val EVENT_SUBSCRIPTION_NAME_COMPLETED = "subscription_name_completed"
        private const val EVENT_SUBSCRIPTION_INTERVAL_STARTED = "subscription_interval_started"
        private const val EVENT_SUBSCRIPTION_INTERVAL_COMPLETED = "subscription_interval_completed"
        private const val EVENT_SUBSCRIPTION_CYCLE_CLICKED = "subscription_cycle_clicked"
        private const val EVENT_SUBSCRIPTION_TRANSACTION_CONFIRM = "subscription_transaction_confirm"
        private const val EVENT_SUBSCRIPTION_TRANSACTION_VIEW = "subscription_transaction_view"
        private const val EVENT_SUBSCRIPTION_TRANSACTION_DETAIL_CLICK = "subscription_tx_details_click"
        private const val EVENT_SUBSCRIPTION_TRANSACTION_DELETE_CLICK = "subscription_delete_click"
        private const val EVENT_SUBSCRIPTION_TRANSACTION_DELETE_CANCEL = "subscription_delete_cancel"
        private const val EVENT_SUBSCRIPTION_TRANSACTION_DELETE_CONFIRM = "subscription_delete_confirm"

        private const val PARAM_SOURCE = "source"
        private const val PARAM_CYCLE = "cycle"
        private const val PARAM_WEEK_VALUE = "week_value"
        private const val PARAM_MONTH_VALUE = "month_value"
        private const val PARAM_SUB_NAME = "sub_name"
        private const val PARAM_SCREEN = "screen"
        private const val PARAM_TRANSACTION_ID = "transaction_id"

        const val RELATIONSHIP_PAGE = "relationship page"
        const val SUBSCRIPTION_PAGE = "subscription page"
    }

    fun trackSubscriptionClick(source: String, accountId: String, mobile: String?) {
        val eventProperties = mapOf<String, Any>(
            PARAM_SOURCE to source,
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_CLICK, eventProperties)
    }

    fun trackSubscriptionStarted(accountId: String, mobile: String?) {
        val eventProperties = mapOf<String, Any>(
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_TRANSACTION_STARTED, eventProperties)
    }

    fun trackSubscriptionAmountEntered(accountId: String, mobile: String?) {
        val eventProperties = mapOf<String, Any>(
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_AMOUNT_ENTERED, eventProperties)
    }

    fun trackSubscriptionNameStarted(accountId: String, mobile: String?) {
        val eventProperties = mapOf<String, Any>(
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_NAME_STARTED, eventProperties)
    }

    fun trackSubscriptionNameCompleted(name: String, accountId: String, mobile: String?) {
        val eventProperties = mapOf<String, Any>(
            PARAM_SUB_NAME to name,
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_NAME_COMPLETED, eventProperties)
    }

    fun trackSubscriptionIntervalStarted(accountId: String, mobile: String?) {
        val eventProperties = mapOf<String, Any>(
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_INTERVAL_STARTED, eventProperties)
    }

    fun trackSubscriptionIntervalCycleClick(cycle: String, accountId: String, mobile: String?) {
        val eventProperties = mapOf<String, Any>(
            PARAM_CYCLE to cycle,
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_CYCLE_CLICKED, eventProperties)
    }

    fun trackSubscriptionIntervalCompleted(
        cycle: String,
        monthValue: String,
        weekValue: String,
        accountId: String,
        mobile: String?
    ) {
        val eventProperties = mapOf<String, Any>(
            PARAM_CYCLE to cycle,
            PARAM_MONTH_VALUE to monthValue,
            PARAM_WEEK_VALUE to weekValue,
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_INTERVAL_COMPLETED, eventProperties)
    }

    fun trackSubscriptionConfirm(
        name: String,
        cycle: String,
        monthValue: String,
        weekValue: String,
        accountId: String,
        mobile: String?
    ) {
        val eventProperties = mapOf<String, Any>(
            PARAM_SUB_NAME to name,
            PARAM_CYCLE to cycle,
            PARAM_MONTH_VALUE to monthValue,
            PARAM_WEEK_VALUE to weekValue,
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_TRANSACTION_CONFIRM, eventProperties)
    }

    fun trackSubscriptionTransactionView(accountId: String, mobile: String?, transactionId: String) {
        val eventProperties = mapOf<String, Any>(
            PARAM_SUB_NAME to transactionId,
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_TRANSACTION_VIEW, eventProperties)
    }

    fun trackSubscriptionTransactionDetailsClick(
        screen: String,
        accountId: String,
        mobile: String?,
        transactionId: String
    ) {
        val eventProperties = mapOf<String, Any>(
            PARAM_TRANSACTION_ID to transactionId,
            PARAM_SCREEN to screen,
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_TRANSACTION_DETAIL_CLICK, eventProperties)
    }

    fun trackSubscriptionDeleteClick(
        screen: String,
        accountId: String,
        mobile: String?,
        transactionId: String
    ) {
        val eventProperties = mapOf<String, Any>(
            PARAM_TRANSACTION_ID to transactionId,
            PARAM_SCREEN to screen,
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_TRANSACTION_DELETE_CLICK, eventProperties)
    }

    fun trackSubscriptionDeleteCancel(
        screen: String,
        accountId: String,
        mobile: String?,
        transactionId: String
    ) {
        val eventProperties = mapOf<String, Any>(
            PARAM_TRANSACTION_ID to transactionId,
            PARAM_SCREEN to screen,
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_TRANSACTION_DELETE_CANCEL, eventProperties)
    }

    fun trackSubscriptionDeleteConfirm(
        screen: String,
        accountId: String,
        mobile: String?,
        transactionId: String
    ) {
        val eventProperties = mapOf<String, Any>(
            PARAM_TRANSACTION_ID to transactionId,
            PARAM_SCREEN to screen,
            PropertyKey.RELATION to CustomerEventTracker.RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(EVENT_SUBSCRIPTION_TRANSACTION_DELETE_CONFIRM, eventProperties)
    }
}
