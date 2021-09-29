package `in`.okcredit.collection_ui.analytics

import `in`.okcredit.analytics.IAnalyticsProvider
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.Event.BILLING_ADD_NEW_ITEM_CLICKED
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.Event.BILLING_BILL_CLICKED
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.Event.BILLING_BILL_ERROR
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.Event.BILLING_BILL_ITEM_ADDED
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.Event.BILLING_BILL_ITEM_REMOVED
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.Event.BILLING_BILL_NEW_ITEM_CLICKED
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.Event.BILLING_CREATE_NEW_BILL_CLICKED
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.Event.BILLING_ITEM_EDITED
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.Event.BILLING_NEW_ITEM_SAVED
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.Event.BILLING_SAVE_BILL_CLICKED
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.Event.BILLING_TAB_CHANGED
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyKey.BILL_ID
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyKey.BILL_TOTAL
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyKey.ERROR_REASON
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyKey.ITEMS_COUNT
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyKey.NAME
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyKey.QUANTITY
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyKey.RATE
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyKey.SCREEN
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyKey.TO
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyKey.TOTAL_QUANTITY
import dagger.Lazy
import javax.inject.Inject

class InventoryEventTracker @Inject constructor(private val analyticsProvider: Lazy<IAnalyticsProvider>) {

    object Event {
        const val BILLING_ADD_NEW_ITEM_CLICKED = "billing_add_new_item_clicked"
        const val BILLING_CREATE_NEW_BILL_CLICKED = "billing_create_new_bill_clicked"
        const val BILLING_TAB_CHANGED = "billing_tab_changed"
        const val BILLING_BILL_NEW_ITEM_CLICKED = "billing_bill_new_item_clicked"
        const val BILLING_NEW_ITEM_SAVED = "billing_new_item_saved"
        const val BILLING_ITEM_EDITED = "billing_item_edited"
        const val BILLING_BILL_ITEM_ADDED = "billing_bill_item_added"
        const val BILLING_BILL_ITEM_REMOVED = "billing_bill_item_removed"
        const val BILLING_SAVE_BILL_CLICKED = "billing_save_bill_clicked"
        const val BILLING_BILL_ERROR = "billing_error"
        const val BILLING_BILL_CLICKED = "billing_bill_clicked"
    }

    object PropertyKey {
        const val TO = "to"
        const val SCREEN = "screen"
        const val NAME = "name"
        const val RATE = "rate"
        const val QUANTITY = "quantity"
        const val BILL_TOTAL = "bill_total"
        const val ITEMS_COUNT = "items_count"
        const val TOTAL_QUANTITY = "total_quantity"
        const val ERROR_REASON = "error_reason"
        const val BILL_ID = "bill_id"
    }

    object PropertyValue {
        const val BILLS = "bills"
        const val ITEMS = "items"
        const val NEW_BILL = "new_bill"
    }

    fun trackBillingAddNewItemClicked() {
        analyticsProvider.get().trackEvents(BILLING_ADD_NEW_ITEM_CLICKED, null)
    }

    fun trackBillingCreateNewBillClicked() {
        analyticsProvider.get().trackEvents(BILLING_CREATE_NEW_BILL_CLICKED, null)
    }

    fun trackBillingTabChanged(to: String) {
        val property = mapOf(
            TO to to,
        )
        analyticsProvider.get().trackEvents(BILLING_TAB_CHANGED, property)
    }

    fun trackBillingNewItemClicked() {
        analyticsProvider.get().trackEvents(BILLING_BILL_NEW_ITEM_CLICKED, null)
    }

    fun trackBillingNewItemSaved(
        screen: String,
        name: String,
        rate: String,
        quantity: String,
    ) {
        val property = mapOf(
            SCREEN to screen,
            NAME to name,
            RATE to rate,
            QUANTITY to quantity,
        )
        analyticsProvider.get().trackEvents(BILLING_NEW_ITEM_SAVED, property)
    }

    fun trackBillingNewItemEdited(
        screen: String,
        name: String,
        rate: String,
        quantity: String,
    ) {
        val property = mapOf(
            SCREEN to screen,
            NAME to name,
            RATE to rate,
            QUANTITY to quantity,
        )
        analyticsProvider.get().trackEvents(BILLING_ITEM_EDITED, property)
    }

    fun trackBillingBillItemAdded(name: String) {
        val property = mapOf(
            NAME to name,
        )
        analyticsProvider.get().trackEvents(BILLING_BILL_ITEM_ADDED, property)
    }

    fun trackBillingBillItemRemoved(name: String) {
        val property = mapOf(
            NAME to name,
        )
        analyticsProvider.get().trackEvents(BILLING_BILL_ITEM_REMOVED, property)
    }

    fun trackBillingSaveBillClicked(
        billTotal: String,
        itemsCount: String,
        totalQuantity: String,
    ) {
        val property = mapOf(
            BILL_TOTAL to billTotal,
            ITEMS_COUNT to itemsCount,
            TOTAL_QUANTITY to totalQuantity,
        )
        analyticsProvider.get().trackEvents(BILLING_SAVE_BILL_CLICKED, property)
    }

    fun trackBillError(errorReason: String) {
        val property = mapOf(
            ERROR_REASON to errorReason,
        )
        analyticsProvider.get().trackEvents(BILLING_BILL_ERROR, property)
    }

    fun trackBillCLicked(billId: String) {
        val property = mapOf(
            BILL_ID to billId,
        )
        analyticsProvider.get().trackEvents(BILLING_BILL_CLICKED, property)
    }
}
