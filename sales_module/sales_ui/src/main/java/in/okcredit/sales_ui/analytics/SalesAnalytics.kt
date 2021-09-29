package `in`.okcredit.sales_ui.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.PropertyKey.RELATION
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.PropertyKey.SCREEN
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.PropertyKey.TYPE
import dagger.Lazy
import javax.inject.Inject

class SalesAnalytics @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    object Event {
        const val IMPORT_CONTACT_CLICKED = "Import Contact Clicked"
        const val VIEW_CONTACT_PERMISSION = "View Contact Permission"
        const val GRANT_PERMISSION = "Grant Permission"
        const val DENY_PERMISSION = "Deny Permission"
        const val IMPORT_CONTACT = "Import Contact"
        const val SELECT_NAME = "Select Name"
        const val CONFIRM_NAME = "Confirm Name"
        const val SELECT_MOBILE = "Select Mobile"
        const val CONFIRM_MOBILE = "Confirm Mobile"
        const val SELECT_CONTACT = "Select Contact"
        const val CONFIRM_BILLING_NAME = "Confirm Billing Name"
        const val CASH_SALE_ITEM_ADD_STARTED = "cashsale_item_add_started"
        const val CASH_SALE_NEW_ITEM_ADD_STARTED = "cashsale_newitem_add_started"
        const val CASH_SALE_ITEM_NAME_ADD = "cashsale_itemname_add"
        const val CASH_SALE_ITEM_PRICE_ADD = "cashsale_itemprice_add"
        const val CASH_SALE_NEW_ITEM_ADD_COMPLETED = "cashsale_newitem_add_completed"
        const val CASH_SALE_NEW_ITEM_ADD_FAILED = "cashsale_newitem_add_failed"
        const val CASH_SALE_ITEM_QUANTITY_EDIT = "cashsale_itemquantity_edit"
        const val CASH_SALE_ITEM_ADD_COMPLETED = "cashsale_item_add_completed"
        const val CASH_SALE_ITEM_SEARCH = "cashsale_item_search"
        const val CASH_SALE_ITEM_SELECT = "cashsale_item_select"
        const val CASH_SALE_ITEM_VIEW = "cashsale_item_view"
        const val CASH_SALE_DELETED = "Cash Sale Deleted"
        const val DELETE_CASH_SALE_CANCELLED = "Delete Cash Sale Cancelled"
        const val CASH_SALE_TRANSACTION_SHARE = "cashsale_transaction_share"
    }

    object PropertyKey {
        const val FLOW = "Flow"
        const val SCREEN = "Screen"
        const val TYPE = "Type"
        const val RELATION = "Relation"
        const val ACCOUNT_ID = "account_id"
        const val SEARCH = "search"
        const val CONTACT = "contact"
        const val FIRST_FLOW = "first_flow"
        const val AMOUNT = "amount"
        const val PRICE = "price"
        const val ITEM_NAME = "item_name"
        const val QUANTITY = "quantity"
    }

    object PropertyValue {
        const val CASH_SALE = "Cash Sale"
        const val CASH_SALE_TX = "Cash Sale Tx"
        const val CONTACT = "Contact"
    }

    fun trackEvents(
        eventName: String,
        type: String? = null,
        screen: String? = null,
        relation: String? = null,
        propertiesMap: PropertiesMap? = null
    ) {

        val properties = propertiesMap?.map() ?: mutableMapOf()

        if (!type.isNullOrEmpty()) {
            properties[TYPE] = type
        }

        if (!screen.isNullOrEmpty()) {
            properties[SCREEN] = screen
        }

        if (!relation.isNullOrEmpty()) {
            properties[RELATION] = relation
        }

        analyticsProvider.get().trackEvents(eventName, properties)
    }
}
