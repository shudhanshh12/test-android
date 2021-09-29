package `in`.okcredit.merchant.customer_ui.ui.staff_link

import `in`.okcredit.analytics.AnalyticsProvider
import dagger.Lazy
import javax.inject.Inject
import kotlin.math.abs

class StaffLinkEventsTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    object Events {
        const val COLLECTIONS_LIST_EDUCATION = "collections_list_education"
        const val COLLECTIONS_LIST_CREATE_LIST = "collections_list_create_list"
        const val COLLECTIONS_LIST_LIST_CLICK = "collections_list_list_click"
        const val COLLECTIONS_LIST_GO_BACK = "collections_list_go_back"
        const val COLLECTIONS_LIST_SEARCH = "collections_list_search"
        const val COLLECTIONS_LIST_SEARCH_START = "collections_list_search_start"
        const val COLLECTIONS_LIST_CUSTOMER_SELECTION = "collections_list_customer_selection"
        const val COLLECTIONS_LIST_ADD_DETAILS_CLICK = "collections_list_add_details_click"
        const val COLLECTIONS_LIST_SHARE_LIST = "collections_list_share_list"
        const val COLLECTIONS_LIST_DELETE_LIST = "collections_list_delete_list"
        const val COLLECTIONS_LIST_DELETE_POPUP_ACTION = "collections_list_delete_popup_action"
        const val COLLECTIONS_LIST_ADD_CUSTOMER = "collections_list_add_customer"
        const val COLLECTIONS_LIST_ADD_CUSTOMER_DETAILS = "collections_list_add_customer_details"
        const val COLLECTIONS_LIST_ENTER_CUSTOMER_DETAILS = "collections_list_enter_customer_details"
        const val COLLECTIONS_LIST_SUBMIT_CUSTOMER_DETAILS = "collections_list_submit_customer_details"
    }

    object Params {
        const val LIST_ID = "list_id"
        const val LIST_CREATED_AT = "list_created_at"
        const val CUSTOMER_COUNT = "customer_count"
        const val SCREEN = "screen"
        const val TYPE = "type"
        const val TOTAL_DUE_AMOUNT = "total_due_amount"
        const val ACTION = "action"
        const val ACCOUNT_ID = "account_id"
        const val FLOW = "flow"
    }

    object Screen {
        const val COLLECTIONS_LIST_FOR_STAFF = "collections_list_for_staff"
        const val LIST_NOT_CREATED = "list_not_created"
        const val HOME = "home"
        const val SEARCH = "search"
        const val UPDATE_CUSTOMER_DETAILS = "update_customer_details"
    }

    object Action {
        const val SELECT = "select"
        const val DESELECT = "deselect"
        const val CANCEL = "cancel"
        const val DELETE = "delete"
    }

    object Type {
        const val ALL = "all"
        const val INDIVIDUAL = "individual"
        const val LIST = "list"
        const val CUSTOMER = "customer"
        const val ADDRESS = "address"
        const val MOBILE = "mobile"

        const val CREATE_CUSTOMER_LIST = "create_customer_list"
        const val SHARE_LIST_WITH_STAFF = "share_list_with_staff"
        const val STAFF_QR_SHARE_LINK = "staff_qr_share_link"
        const val PAYMENT_ENTRY = "payment_entry"
    }

    object Flow {
        const val EDIT = "edit"
        const val NEW = "new"
    }

    fun trackCollectionListEducation(type: String) { // create_customer_list / share_list_with_staff / staff_qr_share_link / payment_entry
        val eventProperties = mapOf<String, Any>(
            Params.TYPE to type,
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_EDUCATION, eventProperties)
    }

    fun trackCreateListClicked() {
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_CREATE_LIST, mapOf())
    }

    fun trackActiveListClicked(linkId: String, listCreatedOn: Long, customerCount: Int) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to linkId,
            Params.LIST_CREATED_AT to listCreatedOn,
            Params.CUSTOMER_COUNT to customerCount,
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_LIST_CLICK, eventProperties)
    }

    fun tracCollectionListGoBack(
        screen: String, // list_not_created / home / search / update_customer_details
        linkId: String? = null,
    ) {
        val eventProperties = mapOf<String, Any>(
            Params.SCREEN to screen,
            Params.LIST_ID to (linkId ?: ""),
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_GO_BACK, eventProperties)
    }

    fun tracCollectionListSearch(linkId: String? = null, customerCount: Int, totalDue: Long) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to (linkId ?: ""),
            Params.TOTAL_DUE_AMOUNT to abs(totalDue),
            Params.CUSTOMER_COUNT to customerCount,
            Params.TYPE to "android"
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_SEARCH, eventProperties)
    }

    fun tracCollectionListSearchStart(linkId: String? = null, customerCount: Int, totalDue: Long) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to (linkId ?: ""),
            Params.TOTAL_DUE_AMOUNT to abs(totalDue),
            Params.CUSTOMER_COUNT to customerCount,
            Params.TYPE to "android"
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_SEARCH_START, eventProperties)
    }

    fun tracCollectionListSelection(
        linkId: String? = null,
        screen: String, // home / search / update_customer_details
        action: String, // select / deselect
        customerCount: Int,
        totalDue: Long,
        type: String, // all / individual
        accountId: String,
    ) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to (linkId ?: ""),
            Params.SCREEN to screen,
            Params.ACTION to action,
            Params.TOTAL_DUE_AMOUNT to abs(totalDue),
            Params.CUSTOMER_COUNT to customerCount,
            Params.TYPE to type,
            Params.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_CUSTOMER_SELECTION, eventProperties)
    }

    fun tracCollectionListAddDetails(linkId: String? = null, customerCount: Int, totalDue: Long) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to (linkId ?: ""),
            Params.TOTAL_DUE_AMOUNT to abs(totalDue),
            Params.CUSTOMER_COUNT to customerCount,
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_ADD_DETAILS_CLICK, eventProperties)
    }

    fun tracCollectionListShareList(
        linkId: String? = null,
        customerCount: Int,
        totalDue: Long,
        screen: String, // home / update_customer_details
    ) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to (linkId ?: ""),
            Params.TOTAL_DUE_AMOUNT to abs(totalDue),
            Params.CUSTOMER_COUNT to customerCount,
            Params.SCREEN to screen,
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_SHARE_LIST, eventProperties)
    }

    fun tracCollectionListDeleteList(linkId: String? = null, customerCount: Int, totalDue: Long) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to (linkId ?: ""),
            Params.TOTAL_DUE_AMOUNT to abs(totalDue),
            Params.CUSTOMER_COUNT to customerCount,
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_DELETE_LIST, eventProperties)
    }

    fun tracCollectionListDeletePopUpAction(
        linkId: String? = null,
        customerCount: Int,
        totalDue: Long,
        action: String, // cancel / delete
        type: String, // customer / list
    ) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to (linkId ?: ""),
            Params.TOTAL_DUE_AMOUNT to abs(totalDue),
            Params.CUSTOMER_COUNT to customerCount,
            Params.TYPE to type,
            Params.ACTION to action,
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_DELETE_POPUP_ACTION, eventProperties)
    }

    fun tracCollectionListAddCustomer(linkId: String) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to linkId,
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_ADD_CUSTOMER, eventProperties)
    }

    fun tracCollectionListAddCustomerDetails(
        linkId: String,
        customerCount: Int,
        totalDue: Long,
        accountId: String,
        flow: String, // new / edit
        type: String, // mobile / address
    ) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to linkId,
            Params.TYPE to type,
            Params.ACCOUNT_ID to accountId,
            Params.CUSTOMER_COUNT to customerCount,
            Params.TOTAL_DUE_AMOUNT to abs(totalDue),
            Params.FLOW to flow,
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_ADD_CUSTOMER_DETAILS, eventProperties)
    }

    fun tracCollectionListEnterCustomerDetails(
        linkId: String,
        customerCount: Int,
        totalDue: Long,
        flow: String, // new / edit
        type: String, // mobile / address
    ) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to linkId,
            Params.TYPE to type,
            Params.CUSTOMER_COUNT to customerCount,
            Params.TOTAL_DUE_AMOUNT to abs(totalDue),
            Params.FLOW to flow,
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_ENTER_CUSTOMER_DETAILS, eventProperties)
    }

    fun tracCollectionListSubmitCustomerDetails(
        linkId: String,
        customerCount: Int,
        totalDue: Long,
        flow: String, // new / edit
        type: String, // mobile / address
    ) {
        val eventProperties = mapOf<String, Any>(
            Params.LIST_ID to linkId,
            Params.TYPE to type,
            Params.CUSTOMER_COUNT to customerCount,
            Params.TOTAL_DUE_AMOUNT to abs(totalDue),
            Params.FLOW to flow,
        )
        analyticsProvider.get().trackEvents(Events.COLLECTIONS_LIST_SUBMIT_CUSTOMER_DETAILS, eventProperties)
    }
}
