package `in`.okcredit.analytics

import `in`.okcredit.analytics.Event.ADD_NOTE_CLICKED
import `in`.okcredit.analytics.Event.ADD_NOTE_COMPLETED
import `in`.okcredit.analytics.Event.ADD_NOTE_STARTED
import `in`.okcredit.analytics.Event.ADD_RECEIPT_COMPLETED
import `in`.okcredit.analytics.Event.ADD_RECEIPT_STARTED
import `in`.okcredit.analytics.Event.ADD_RELATIONSHIP
import `in`.okcredit.analytics.Event.ADD_TRANSACTION_STARTED
import `in`.okcredit.analytics.Event.APP_LOCK_DISABLED
import `in`.okcredit.analytics.Event.APP_LOCK_ENABLED
import `in`.okcredit.analytics.Event.CALL_RELATIONSHIP
import `in`.okcredit.analytics.Event.CONFIRM_MOBILE_CHANGE
import `in`.okcredit.analytics.Event.CONFIRM_NAME
import `in`.okcredit.analytics.Event.CONTACT_OKCREDIT
import `in`.okcredit.analytics.Event.DEBUG
import `in`.okcredit.analytics.Event.DELETE_DISCOUNT
import `in`.okcredit.analytics.Event.DELETE_RECEIPT
import `in`.okcredit.analytics.Event.DELETE_RELATIONSHIP
import `in`.okcredit.analytics.Event.DELETE_TRANSACTION
import `in`.okcredit.analytics.Event.DENY_PERMISSION
import `in`.okcredit.analytics.Event.DEVICE_LOCK_SETTING
import `in`.okcredit.analytics.Event.EDIT_RECEIPT
import `in`.okcredit.analytics.Event.ENTER_AMOUNT_CASHBACK_PAGE_VIEW
import `in`.okcredit.analytics.Event.ENTRY_POINT_CLICKED
import `in`.okcredit.analytics.Event.ENTRY_POINT_VIEWED
import `in`.okcredit.analytics.Event.FILE_UPLOAD_ERROR
import `in`.okcredit.analytics.Event.GRANT_PERMISSION
import `in`.okcredit.analytics.Event.IMPORT_CONTACT
import `in`.okcredit.analytics.Event.IN_APP_NOTI_CLEARED
import `in`.okcredit.analytics.Event.IN_APP_NOTI_CLICKED
import `in`.okcredit.analytics.Event.IN_APP_NOTI_DISPLAYED
import `in`.okcredit.analytics.Event.KYC_BANNER_SHOWN
import `in`.okcredit.analytics.Event.KYC_DIALOG_POPUP
import `in`.okcredit.analytics.Event.KYC_ENTRY_POINT_DISMISSED
import `in`.okcredit.analytics.Event.PAY_ONLINE_CASHBACK_PAGE_VIEW
import `in`.okcredit.analytics.Event.PAY_ONLINE_PAGE_VIEW
import `in`.okcredit.analytics.Event.PERMISSION_DENIED
import `in`.okcredit.analytics.Event.PERMISSION_GRANTED
import `in`.okcredit.analytics.Event.REFRESH
import `in`.okcredit.analytics.Event.RX_ERRORHANDLER
import `in`.okcredit.analytics.Event.SEARCH_CATEGORY
import `in`.okcredit.analytics.Event.SEARCH_RELATIONSHIP
import `in`.okcredit.analytics.Event.SECURITY_SCREEN_APP_LOCK_ENABLE_CLICKCED
import `in`.okcredit.analytics.Event.SELECT_LANGUAGE
import `in`.okcredit.analytics.Event.SELECT_MOBILE
import `in`.okcredit.analytics.Event.SELECT_NAME
import `in`.okcredit.analytics.Event.SELECT_PROFILE
import `in`.okcredit.analytics.Event.SELECT_RATING
import `in`.okcredit.analytics.Event.SEND_COLLECTION_REMINDER
import `in`.okcredit.analytics.Event.SEND_REMINDER
import `in`.okcredit.analytics.Event.SHARED
import `in`.okcredit.analytics.Event.START_KYC_CLICKED
import `in`.okcredit.analytics.Event.STOP_LIVESALE_QR
import `in`.okcredit.analytics.Event.SUBMIT_FEEBBACK
import `in`.okcredit.analytics.Event.SUBMIT_FEEDBACK
import `in`.okcredit.analytics.Event.SYNC_COMPLETED
import `in`.okcredit.analytics.Event.SYNC_DIRTY_TRANSACTION
import `in`.okcredit.analytics.Event.SYNC_ERROR
import `in`.okcredit.analytics.Event.SYNC_RESTART
import `in`.okcredit.analytics.Event.SYNC_STARTED
import `in`.okcredit.analytics.Event.UPDATE_PPROFILE
import `in`.okcredit.analytics.Event.VIEW_COMMON_LEDGER
import `in`.okcredit.analytics.Event.VIEW_CONTACT_PERMISSION
import `in`.okcredit.analytics.Event.VIEW_DISCOUNT
import `in`.okcredit.analytics.Event.VIEW_HELP_ITEM
import `in`.okcredit.analytics.Event.VIEW_HELP_TOPIC
import `in`.okcredit.analytics.Event.VIEW_LIVESALES
import `in`.okcredit.analytics.Event.VIEW_PRIVACY
import `in`.okcredit.analytics.Event.VIEW_PROFILE
import `in`.okcredit.analytics.Event.VIEW_RELATIONSHIP
import `in`.okcredit.analytics.Event.VIEW_TRANSACTION
import `in`.okcredit.analytics.Event.YOUTUBE_VIDEO
import `in`.okcredit.analytics.IdentityProperties.MERCHANT_ID
import `in`.okcredit.analytics.PropertyKey.ACCOUNT_ID
import `in`.okcredit.analytics.PropertyKey.ALL
import `in`.okcredit.analytics.PropertyKey.ALWAYS
import `in`.okcredit.analytics.PropertyKey.BLOCKED
import `in`.okcredit.analytics.PropertyKey.BUSINESS_TYPE_ID
import `in`.okcredit.analytics.PropertyKey.BillDate
import `in`.okcredit.analytics.PropertyKey.CASHBACK_AMOUNT
import `in`.okcredit.analytics.PropertyKey.CASHBACK_DUE_AMOUNT
import `in`.okcredit.analytics.PropertyKey.CASHBACK_MESSAGE_TYPE
import `in`.okcredit.analytics.PropertyKey.CASHBACK_MSG_SHOWN
import `in`.okcredit.analytics.PropertyKey.CATEGORY_ID
import `in`.okcredit.analytics.PropertyKey.CHANNEL
import `in`.okcredit.analytics.PropertyKey.COMMON_LEDGER
import `in`.okcredit.analytics.PropertyKey.CONTENT
import `in`.okcredit.analytics.PropertyKey.CUSTOMER_ID
import `in`.okcredit.analytics.PropertyKey.DAILY_LIMIT_LEFT
import `in`.okcredit.analytics.PropertyKey.DATE_TYPE
import `in`.okcredit.analytics.PropertyKey.DEFAULT
import `in`.okcredit.analytics.PropertyKey.DUE_AMOUNT
import `in`.okcredit.analytics.PropertyKey.DUE_RANGE
import `in`.okcredit.analytics.PropertyKey.ENABLED
import `in`.okcredit.analytics.PropertyKey.ERROR
import `in`.okcredit.analytics.PropertyKey.FEEDBACK
import `in`.okcredit.analytics.PropertyKey.FIELD
import `in`.okcredit.analytics.PropertyKey.FLOW
import `in`.okcredit.analytics.PropertyKey.FOCAL_AREA
import `in`.okcredit.analytics.PropertyKey.FORMAT
import `in`.okcredit.analytics.PropertyKey.GPS
import `in`.okcredit.analytics.PropertyKey.INTERACTION
import `in`.okcredit.analytics.PropertyKey.IS_FILE
import `in`.okcredit.analytics.PropertyKey.KYC_MESSAGE_TYPE
import `in`.okcredit.analytics.PropertyKey.LIST
import `in`.okcredit.analytics.PropertyKey.LOCAL_TXN_ID
import `in`.okcredit.analytics.PropertyKey.METHOD
import `in`.okcredit.analytics.PropertyKey.MIGRATING_RELATION
import `in`.okcredit.analytics.PropertyKey.MINIMUM_PAYMENT_AMOUNT
import `in`.okcredit.analytics.PropertyKey.ORIGIN_VALUE
import `in`.okcredit.analytics.PropertyKey.PACKAGE_ID
import `in`.okcredit.analytics.PropertyKey.PLATFORM
import `in`.okcredit.analytics.PropertyKey.POSITION
import `in`.okcredit.analytics.PropertyKey.RELATION
import `in`.okcredit.analytics.PropertyKey.REMOVED
import `in`.okcredit.analytics.PropertyKey.RISK_TYPE
import `in`.okcredit.analytics.PropertyKey.SCREEN
import `in`.okcredit.analytics.PropertyKey.SEARCH
import `in`.okcredit.analytics.PropertyKey.SERVICE
import `in`.okcredit.analytics.PropertyKey.SET_VALUE
import `in`.okcredit.analytics.PropertyKey.SHARE_TYPE
import `in`.okcredit.analytics.PropertyKey.SHOW_IMAGE
import `in`.okcredit.analytics.PropertyKey.SORT_BY
import `in`.okcredit.analytics.PropertyKey.SOURCE
import `in`.okcredit.analytics.PropertyKey.SUPPLIER_LIST
import `in`.okcredit.analytics.PropertyKey.TARGET
import `in`.okcredit.analytics.PropertyKey.TIMESTAMP
import `in`.okcredit.analytics.PropertyKey.TOTAL_COUNT
import `in`.okcredit.analytics.PropertyKey.TXN_ID
import `in`.okcredit.analytics.PropertyKey.TYPE
import `in`.okcredit.analytics.PropertyKey.UNREAD_COUNT
import `in`.okcredit.analytics.PropertyKey.VALUE
import `in`.okcredit.analytics.PropertyKey.VIDEO_ID
import `in`.okcredit.analytics.PropertyKey.VIDEO_TYPE
import `in`.okcredit.analytics.PropertyValue.AMOUNT
import `in`.okcredit.analytics.PropertyValue.CAUSE
import `in`.okcredit.analytics.PropertyValue.CONTACT
import `in`.okcredit.analytics.PropertyValue.CUSTOMER
import `in`.okcredit.analytics.PropertyValue.CUSTOMER_SYNC_STATUS
import `in`.okcredit.analytics.PropertyValue.DEFAULT_MODE
import `in`.okcredit.analytics.PropertyValue.DUE_DATE
import `in`.okcredit.analytics.PropertyValue.MERCHANT
import `in`.okcredit.analytics.PropertyValue.NAME
import `in`.okcredit.analytics.PropertyValue.OLD_DUE_DATE
import `in`.okcredit.analytics.PropertyValue.REASON
import `in`.okcredit.analytics.PropertyValue.RELATIONSHIP
import `in`.okcredit.analytics.PropertyValue.SEARCH_CONTACT
import `in`.okcredit.analytics.PropertyValue.STACKTRACE
import `in`.okcredit.analytics.PropertyValue.STATUS
import `in`.okcredit.analytics.PropertyValue.SUPPLIER
import `in`.okcredit.analytics.PropertyValue.TRANSACTION_DETAILS
import `in`.okcredit.analytics.PropertyValue.TRUE
import `in`.okcredit.analytics.PropertyValue.TX_PERMISSION
import dagger.Lazy
import merchant.android.okstream.contract.OkStreamService
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.utils.getStringStackTrace
import javax.inject.Inject

@Deprecated(message = "https://okcredit.slack.com/archives/GTQMX24MA/p1590195861044100")
@AppScope
class Tracker @Inject constructor(
    private val analyticsProvider: Lazy<IAnalyticsProvider>,
    private val okStreamService: Lazy<OkStreamService>,
) {

    fun setIdentity(id: String, isSignup: Boolean) {
        analyticsProvider.get().setIdentity(id, isSignup)
    }

    fun clearIdentity() {
        analyticsProvider.get().clearIdentity()
    }

    fun setUserProperties(
        merchantId: String,
        name: String?,
        language: String? = null,
        language_device: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[UserProperties.MERCHANT_ID] = merchantId
        if (name != null) properties[UserProperties.NAME] = name
        if (language != null) properties[UserProperties.LANGUAGE] = language
        if (language_device != null) properties[UserProperties.LANGUAGE_DEVICE] = language_device
        analyticsProvider.get().setUserProperty(properties)
    }

    fun registerSuperPropertiesForCustomersAndTransactionsCount(totalTxnCount: Int, totalCustomerCount: Int) {
        val properties = mutableMapOf<String, Any>()
        properties[UserProperties.CUSTOMER_TRANSACTION_COUNT] = totalTxnCount
        properties[UserProperties.CUSTOMER_COUNT] = totalCustomerCount
        analyticsProvider.get().registerSuperProperties(properties)
    }

    fun setUserProperty(key: String, value: String) {
        val properties = mutableMapOf<String, Any>()
        properties[key] = value
        analyticsProvider.get().setUserProperty(properties)
    }

    fun setSuperProperties(key: String, value: String) {
        val properties = mutableMapOf<String, Any>()
        properties[key] = value
        analyticsProvider.get().setSuperProperties(properties)
    }

    fun setSuperPropertiesForIndividual(individualId: String, lang: String? = null) {
        val properties = mutableMapOf<String, Any>()
        properties[SuperProperties.INDIVIDUAL_ID] = individualId
        if (lang != null) properties[SuperProperties.LANG] = lang
        analyticsProvider.get().setSuperProperties(properties)
    }

    fun setLangSuperProperty(lang: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SuperProperties.LANG] = lang
        analyticsProvider.get().setSuperProperties(properties)
    }

    fun setVersionSuperProperty(version: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SuperProperties.OKCREDIT_VERTSION] = version
        analyticsProvider.get().setSuperProperties(properties)
    }

    fun incrementSuperProperty(type: String) {
        when (type) {
            SuperProperties.CUSTOMER_COUNT -> analyticsProvider.get().incrementCustomerCountSuperProperty()
            SuperProperties.CUSTOMER_TRANSACTION_COUNT -> analyticsProvider.get()
                .incrementTransactionCountSuperProperty()
        }
    }

    fun trackError(screen: String, type: String, reason: String = "", relation: String? = null) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        if (reason.isNotBlank()) properties[REASON] = reason
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        analyticsProvider.get().trackEvents(ERROR, properties)
    }

    fun trackError(screen: String, type: String, reason: Throwable?) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        if (reason != null) properties[REASON] = reason.getStringStackTrace()
        analyticsProvider.get().trackEvents(ERROR, properties)
    }

    fun trackRxUnHandledError(type: String, reason: Throwable?) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        properties[REASON] = reason?.message ?: ""
        properties[CAUSE] = reason?.cause?.message ?: ""
        if (reason != null) properties[STACKTRACE] = reason.getStringStackTrace()
        if ((reason?.message ?: "").contains("NetworkError").not()) {
            analyticsProvider.get().trackEngineeringMetricEvents(RX_ERRORHANDLER, properties)
        }
    }

    fun track(event: String) {
        analyticsProvider.get().trackEvents(event, null)
    }

    fun trackV1(event: String, screen: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(event, properties)
    }

    fun trackEvents(
        eventName: String,
        type: String? = null,
        screen: String? = null,
        relation: String? = null,
        source: String? = null,
        value: Boolean? = null,
        propertiesMap: PropertiesMap? = null,
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

        if (value != null) {
            properties[VALUE] = value
        }

        if (!source.isNullOrEmpty()) {
            properties[SOURCE] = source
        }

        analyticsProvider.get().trackEvents(eventName, properties)
    }

    fun trackAddRelationshipStartedFlows(
        screen: String? = null,
        relation: String,
        accountId: String? = null,
        search: String,
        type: String,
        contact: String,
        source: String? = null,
        flow: String? = null,
        defaultMode: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!screen.isNullOrBlank()) {
            properties[SCREEN] = screen
        }

        properties[RELATION] = relation
        properties[SEARCH] = search
        properties[TYPE] = type
        properties[CONTACT] = contact
        if (!accountId.isNullOrBlank()) {
            properties[ACCOUNT_ID] = accountId
        }
        if (!source.isNullOrEmpty()) {
            properties[SOURCE] = source
        }
        if (!flow.isNullOrEmpty()) {
            properties[FLOW] = flow
        }
        if (!defaultMode.isNullOrEmpty()) {
            properties[DEFAULT_MODE] = defaultMode
        }
        analyticsProvider.get().trackEvents(Event.ADD_RELATIONSHIP_STARTED, properties)
    }

    fun trackAddRelationshipSuccessFlows(
        screen: String? = null,
        relation: String,
        accountId: String? = null,
        search: String,
        contact: String,
        searchContact: String? = null,
        flow: String? = null,
        customerSyncStatus: String? = null,
        defaultMode: String? = null,
        source: String? = null
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!screen.isNullOrBlank()) {
            properties[SCREEN] = screen
        }

        properties[RELATION] = relation
        properties[SEARCH] = search
        properties[CONTACT] = contact
        if (!accountId.isNullOrBlank()) {
            properties[ACCOUNT_ID] = accountId
        }

        if (!searchContact.isNullOrBlank()) {
            properties[SEARCH_CONTACT] = searchContact
        }
        if (!flow.isNullOrBlank()) {
            properties[FLOW] = flow
        }
        if (!customerSyncStatus.isNullOrBlank()) {
            properties[CUSTOMER_SYNC_STATUS] = customerSyncStatus
        }

        if (!defaultMode.isNullOrEmpty()) {
            properties[DEFAULT_MODE] = defaultMode
        }
        if (!source.isNullOrEmpty()) {
            properties[SOURCE] = source
        }

        analyticsProvider.get().trackEvents(Event.ADD_RELATIONSHIP_SUCCESS, properties)
        // FIXME:: this just for v0 testing
        if (relation == CUSTOMER) {
            okStreamService.get().publishAddCustomerSuccess(properties, accountId ?: "")
        }
    }

    fun trackViewRelationship(
        list: String,
        relation: String,
        search: String,
        accountId: String? = null,
        flow: String? = null,
        commonLedger: Boolean,
        customerSyncStatus: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[LIST] = list
        properties[RELATION] = relation
        properties[SEARCH] = search
        if (!accountId.isNullOrBlank()) {
            properties[ACCOUNT_ID] = accountId
        }
        if (!flow.isNullOrBlank()) {
            properties[FLOW] = flow
        }
        if (!customerSyncStatus.isNullOrBlank()) {
            properties[CUSTOMER_SYNC_STATUS] = customerSyncStatus
        }
        properties[COMMON_LEDGER] = commonLedger
        analyticsProvider.get().trackEvents(VIEW_RELATIONSHIP, properties)
    }

    fun trackViewRelationshipV1(
        list: String,
        relation: String,
        search: String,
        screen: String,
        accountId: String? = null,
        commonLedger: Boolean,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[LIST] = list
        properties[RELATION] = relation
        properties[SEARCH] = search
        properties[SCREEN] = screen
        if (!accountId.isNullOrBlank()) {
            properties[ACCOUNT_ID] = accountId
        }
        properties[COMMON_LEDGER] = commonLedger
        analyticsProvider.get().trackEvents(VIEW_RELATIONSHIP, properties)
    }

    fun trackSearchRelationship(relation: String) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        analyticsProvider.get().trackEvents(SEARCH_RELATIONSHIP, properties)
    }

    fun trackAddTransactionSearchShortcutRelationship(source: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SOURCE] = source
        analyticsProvider.get().trackEvents(SEARCH_RELATIONSHIP, properties)
    }

    fun trackViewProfile(screen: String, type: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        analyticsProvider.get().trackEvents(VIEW_PROFILE, properties)
    }

    fun trackViewProfile(
        screen: String,
        type: String,
        relation: String,
        accountId: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        properties[RELATION] = relation
        if (!accountId.isNullOrBlank()) {
            properties[ACCOUNT_ID] = accountId
        }
        analyticsProvider.get().trackEvents(VIEW_PROFILE, properties)
    }

    fun trackRefresh(type: String) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        analyticsProvider.get().trackEvents(REFRESH, properties)
    }

    fun trackImportContact(screen: String, relation: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[RELATION] = relation
        analyticsProvider.get().trackEvents(IMPORT_CONTACT, properties)
    }

    fun trackRuntimePermission(screen: String, type: String, granted: Boolean) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        if (granted)
            analyticsProvider.get().trackEvents(GRANT_PERMISSION, properties)
        else
            analyticsProvider.get().trackEvents(DENY_PERMISSION, properties)
    }

    fun trackAddRelationship(
        screen: String? = null,
        relation: String,
        type: String,
        contact: String,
        source: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!screen.isNullOrBlank()) {
            properties[SCREEN] = screen
        }
        properties[RELATION] = relation
        properties[TYPE] = type
        properties[CONTACT] = contact
        if (contact == TRUE) {
            properties[RELATION] = ADD_RELATIONSHIP
        } else {
            properties[FLOW] = ADD_RELATIONSHIP
        }

        if (!source.isNullOrEmpty()) {
            properties[SOURCE] = source
        }

        analyticsProvider.get().trackEvents(ADD_RELATIONSHIP, properties)
    }

    fun trackContactOkCredit(screen: String, type: String, source: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        properties[SOURCE] = source
        analyticsProvider.get().trackEvents(CONTACT_OKCREDIT, properties)
    }

    fun trackViewHelpTopic_v2(type: String?, source: String?, interaction: String?) {
        val properties = mutableMapOf<String, Any>()
        if (type != null) properties[TYPE] = type
        if (source != null) properties[SOURCE] = source
        if (interaction != null) properties[INTERACTION] = interaction
        analyticsProvider.get().trackEvents(VIEW_HELP_TOPIC, properties)
    }

    @NonNls
    fun trackViewHelpItem_v2(
        type: String?,
        screen: String = "NA",
        source: String?,
        interaction: String,
        method: String,
        format: String,
        error: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (type != null) properties[TYPE] = type
        properties[SCREEN] = screen
        if (source != null) properties[SOURCE] = source
        properties[INTERACTION] = interaction
        properties[METHOD] = method
        properties[FORMAT] = format
        if (error != null) properties[ERROR] = error
        analyticsProvider.get().trackEvents(VIEW_HELP_ITEM, properties)
    }

    fun trackSupplierEducationVideo(
        type: String?,
        screen: String = "NA",
        source: String?,
        interaction: String,
        method: String,
        format: String,
        error: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (type != null) properties[TYPE] = type
        properties[SCREEN] = screen
        if (source != null) properties[SOURCE] = source
        properties[INTERACTION] = interaction
        properties[METHOD] = method
        properties[FORMAT] = format
        if (error != null) properties[ERROR] = error
        analyticsProvider.get().trackEvents(VIEW_HELP_ITEM, properties)
    }

    fun trackSelectLanguage(setValue: String, originValue: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SET_VALUE] = setValue
        properties[ORIGIN_VALUE] = originValue
        analyticsProvider.get().trackEvents(SELECT_LANGUAGE, properties)
    }

    fun trackUpdateProfileV1(relation: String, field: String, accountId: String? = null) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[FIELD] = field
        if (relation != MERCHANT) { // not sending this property for merchant

            if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        }
        analyticsProvider.get().trackEvents(UPDATE_PPROFILE, properties)
    }

    fun trackUpdateProfileV2(relation: String, field: String, gps: String) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[FIELD] = field
        properties[GPS] = gps
        analyticsProvider.get().trackEvents(UPDATE_PPROFILE, properties)
    }

    fun trackUpdateProfileV3(
        relation: String,
        field: String,
        setValue: String,
        accountId: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[FIELD] = field
        properties[SET_VALUE] = setValue
        properties[ACCOUNT_ID] = accountId
        analyticsProvider.get().trackEvents(UPDATE_PPROFILE, properties)
    }

    fun trackUpdateProfileV4(
        relation: String,
        field: String,
        setValue: String,
        default: String,
        accountId: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[FIELD] = field
        properties[SET_VALUE] = setValue
        properties[DEFAULT] = default
        properties[ACCOUNT_ID] = accountId
        analyticsProvider.get().trackEvents(UPDATE_PPROFILE, properties)
    }

    fun trackUpdateProfileV5(
        relation: String,
        field: String,
        method: String,
        accountId: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[FIELD] = field
        properties[METHOD] = method
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        analyticsProvider.get().trackEvents(UPDATE_PPROFILE, properties)
    }

    fun trackUpdateProfileV6(
        relation: String,
        field: String,
        method: String,
        setValue: String? = null,
        removed: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[FIELD] = field
        properties[METHOD] = method
        if (!setValue.isNullOrBlank()) properties[SET_VALUE] = setValue
        properties[REMOVED] = removed
        analyticsProvider.get().trackEvents(UPDATE_PPROFILE, properties)
    }

    fun trackUpdateProfileV7(
        relation: String,
        field: String,
        accountId: String? = null,
        removed: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[FIELD] = field
        properties[REMOVED] = removed
        if (relation != MERCHANT) { // not sending this property for merchant
            if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        }
        analyticsProvider.get().trackEvents(UPDATE_PPROFILE, properties)
    }

    fun trackUpdateProfileLegacy(
        relation: String? = null,
        field: String? = null,
        setValue: String? = null,
        removed: Boolean? = null,
        method: String? = null,
        categoryId: String? = null,
        businessId: String? = null,
        search: String? = null,
        type: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        relation?.let { properties[RELATION] = it }
        field?.let { properties[FIELD] = it }
        setValue?.let { properties[SET_VALUE] = it }
        removed?.let { properties[REMOVED] = it }
        method?.let { properties[METHOD] = it }
        categoryId?.let { properties[CATEGORY_ID] = it }
        businessId?.let { properties[BUSINESS_TYPE_ID] = it }
        search?.let { properties[SEARCH] = it }
        type?.let { properties[TYPE] = it }
        analyticsProvider.get().trackEvents(UPDATE_PPROFILE, properties)
    }

    fun trackSearchCategory(
        relation: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        relation?.let { properties[RELATION] = it }
        analyticsProvider.get().trackEvents(SEARCH_CATEGORY, properties)
    }

    fun trackSelectProfileV1(
        relation: String,
        type: String = "",
        field: String,
        accountId: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[FIELD] = field
        if (type.isNotBlank()) {
            properties[TYPE] = type
        }
        if (relation != MERCHANT) { // not sending this property for merchant

            if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        }

        analyticsProvider.get().trackEvents(SELECT_PROFILE, properties)
    }

    fun trackSelectProfileV2(
        screen: String,
        type: String,
        field: String,
        relation: String,
        accountId: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        properties[FIELD] = field
        properties[RELATION] = relation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        analyticsProvider.get().trackEvents(SELECT_PROFILE, properties)
    }

    fun trackDeleteRelationship(
        type: String,
        accountId: String? = null,
        customerSyncStatus: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        if (!customerSyncStatus.isNullOrBlank()) properties[CUSTOMER_SYNC_STATUS] = customerSyncStatus
        analyticsProvider.get().trackEvents(DELETE_RELATIONSHIP, properties)
    }

    fun trackYoutube(screen: String, type: String, videoId: String, error: String = "") {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        properties[VIDEO_ID] = videoId
        properties[ERROR] = error
        analyticsProvider.get().trackEvents(YOUTUBE_VIDEO, properties)
    }

    fun trackCallRelationShip(
        screen: String,
        relation: String,
        accountId: String? = null,
        isBlocked: Boolean = false,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[RELATION] = relation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[BLOCKED] = isBlocked
        analyticsProvider.get().trackEvents(CALL_RELATIONSHIP, properties)
    }

    fun trackViewTransaction(
        screen: String,
        relation: String,
        accountId: String? = null,
        flow: String? = null,
        type: String? = null,
        status: String? = null,
        blocked: Boolean? = false,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[RELATION] = relation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!type.isNullOrBlank()) properties[TYPE] = type
        if (!status.isNullOrBlank()) properties[STATUS] = status
        if (blocked != null) properties[BLOCKED] = blocked
        analyticsProvider.get().trackEvents(VIEW_TRANSACTION, properties)
    }

    fun trackViewDiscount(screen: String, relation: String, accountId: String? = null) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[RELATION] = relation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        analyticsProvider.get().trackEvents(VIEW_DISCOUNT, properties)
    }

    fun trackViewPrivacy(screen: String, relation: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[RELATION] = relation
        analyticsProvider.get().trackEvents(VIEW_PRIVACY, properties)
    }

    fun trackAddTransactionFlowsStarted(
        type: String,
        relation: String,
        accountId: String? = null,
        source: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        properties[RELATION] = relation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[SOURCE] = source
        analyticsProvider.get().trackEvents(ADD_TRANSACTION_STARTED, properties)
    }

    fun trackInAppDisplayed(type: String, screen: String? = null, source: String? = null) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        if (screen.isNullOrBlank().not()) {
            properties[SCREEN] = screen!!
        }
        if (!source.isNullOrBlank()) properties[SOURCE] = source
        analyticsProvider.get().trackEvents(IN_APP_NOTI_DISPLAYED, properties)
    }

    fun trackInAppDisplayedV1(type: String, screen: String? = null) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE.toLowerCase()] = type
        if (screen.isNullOrBlank().not()) {
            properties[SCREEN.toLowerCase()] = screen!!
        }
        analyticsProvider.get().trackEvents(IN_APP_NOTI_DISPLAYED, properties)
    }

    fun trackInAppClicked(type: String, value: Boolean, source: String? = null) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        properties[VALUE] = value
        if (!source.isNullOrBlank()) properties[SOURCE] = source
        analyticsProvider.get().trackEvents(IN_APP_NOTI_CLICKED, properties)
    }

    fun trackInAppClickedV1(type: String, screen: String) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE.toLowerCase()] = type
        properties[SCREEN.toLowerCase()] = screen
        analyticsProvider.get().trackEvents(IN_APP_NOTI_CLICKED, properties)
    }

    fun trackInAppClickedV2(type: String, screen: String, focalArea: String, value: String? = null) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        properties[SCREEN] = screen
        properties[FOCAL_AREA] = focalArea
        if (value.isNullOrBlank().not()) {
            properties[VALUE] = value!!
        }
        analyticsProvider.get().trackEvents(IN_APP_NOTI_CLICKED, properties)
    }

    fun trackInAppCleared(type: String, screen: String, method: String) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE.toLowerCase()] = type
        properties[SCREEN.toLowerCase()] = screen
        properties[METHOD] = method
        analyticsProvider.get().trackEvents(IN_APP_NOTI_CLEARED, properties)
    }

    fun trackInAppClearedV1(type: String, method: String) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        properties[METHOD] = method
        analyticsProvider.get().trackEvents(IN_APP_NOTI_CLEARED, properties)
    }

    fun trackDeleteTransaction(accountId: String? = null, txnId: String?, relation: String) {
        val properties = mutableMapOf<String, Any>()
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        if (!txnId.isNullOrBlank()) properties[TXN_ID] = txnId
        properties[RELATION] = relation
        analyticsProvider.get().trackEvents(DELETE_TRANSACTION, properties)
    }

    fun trackDeleteDiscount(accountId: String? = null, txnId: String?, relation: String) {
        val properties = mutableMapOf<String, Any>()
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        if (!txnId.isNullOrBlank()) properties[TXN_ID] = txnId
        properties[RELATION] = relation
        analyticsProvider.get().trackEvents(DELETE_DISCOUNT, properties)
    }

    fun trackViewQr(screen: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.VIEW_QR, properties)
    }

    fun trackViewLiveSaleQr(screen: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.VIEW_LIVESALES_QR, properties)
    }

    fun trackInvalidBankDetails(type: String, isEdit: Boolean) {
        val properties = mutableMapOf<String, Any>()
        properties[PropertyKey.IS_EDIT] = isEdit
        properties[TYPE] = type
        analyticsProvider.get().trackEvents(Event.INVALID_BANK_DETAILS, properties)
    }

    fun trackCollectionBulkRemainder(selectAll: Boolean, sendCount: Int, totalCount: Int) {
        val properties = mutableMapOf<String, Any>()
        properties[PropertyKey.SELECT_ALL] = selectAll
        properties[PropertyKey.SEND_COUNT] = sendCount
        properties[TOTAL_COUNT] = totalCount
        analyticsProvider.get().trackEvents(Event.COLLECTION_BULK_REMINDER, properties)
    }

    fun trackViewCollectionBulkReminder(source: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SOURCE] = source
        analyticsProvider.get().trackEvents(Event.VIEW_COLLECTION_BULK, properties)
    }

    fun trackSendReminder(
        type: String,
        customerId: String,
        txnId: String,
        screen: String,
        relation: String,
        mobile: String? = null,
        action: String = "",
        balance: String? = "",
        cashbackMsgShown: Boolean? = null,
        dueAmount: String = "",
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        properties[RELATION] = relation
        properties[SCREEN] = screen
        properties[CUSTOMER_ID] = customerId
        properties[ACCOUNT_ID] = customerId
        properties[TXN_ID] = txnId
        properties[ENABLED] = mobile.isNullOrBlank().not()
        cashbackMsgShown?.let {
            properties[CASHBACK_MSG_SHOWN] = if (it) PropertyValue.YES else PropertyValue.NO
            properties[CASHBACK_DUE_AMOUNT] = dueAmount
        }
        if (action != "") properties[PropertyKey.ACTION] = action
        if (!balance.isNullOrEmpty()) properties[PropertyKey.BALANCE] = balance
        analyticsProvider.get().trackEvents(SEND_REMINDER, properties)
    }

    fun trackSendPaymentReminder(
        type: String,
        screen: String,
        showImage: Boolean,
        customerId: String,
        action: String = "",
        cashbackMsgShown: Boolean? = null,
        dueAmount: String = "",
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[CUSTOMER_ID] = customerId
        properties[ACCOUNT_ID] = customerId
        properties[TYPE] = type
        properties[SCREEN] = screen
        properties[SHOW_IMAGE] = showImage
        properties[PropertyKey.ACTION] = action
        cashbackMsgShown?.let {
            properties[CASHBACK_MSG_SHOWN] = if (it) PropertyValue.YES else PropertyValue.NO
            properties[CASHBACK_DUE_AMOUNT] = dueAmount
        }
        analyticsProvider.get().trackEvents(SEND_COLLECTION_REMINDER, properties)
    }

    fun trackConfirmNumberChange(numberChange: String, old: String, all: String?) {
        val properties = mutableMapOf<String, Any>()
        properties[FLOW] = numberChange
        properties[VALUE] = old
        if (!all.isNullOrBlank()) properties[ALL] = all
        analyticsProvider.get().trackEvents(CONFIRM_MOBILE_CHANGE, properties)
    }

    fun requestOTP(flow: String, screen: String) {
        val properties = mutableMapOf<String, Any>()
        properties[FLOW] = flow
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.REQUEST_OTP, properties)
    }

    fun trackSyncStarted() {
        analyticsProvider.get().trackEngineeringMetricEvents(SYNC_STARTED, null)
    }

    fun trackSyncCompleted(isFile: Boolean) {
        val properties = mutableMapOf<String, Any>()
        properties[IS_FILE] = isFile
        analyticsProvider.get().trackEngineeringMetricEvents(SYNC_COMPLETED, properties)
    }

    fun trackSyncError(isFile: Boolean, type: String, reason: String?, stackTrace: String?) {
        val properties = mutableMapOf<String, Any>()
        properties[IS_FILE] = isFile
        properties[TYPE] = type
        properties[REASON] = reason ?: ""
        properties[STACKTRACE] = stackTrace ?: ""
        analyticsProvider.get().trackEngineeringMetricEvents(SYNC_ERROR, properties)
    }

    fun trackSyncRestart(method: String) {
        val properties = mutableMapOf<String, Any>()
        properties[METHOD] = method
        analyticsProvider.get().trackEngineeringMetricEvents(SYNC_RESTART, properties)
    }

    fun trackSyncTransactions(step: String, count: Int) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = "SyncTransactionsFile"
        properties["Step"] = step
        properties["Txn Count"] = count
        analyticsProvider.get().trackEngineeringMetricEvents(DEBUG, properties)
    }

    fun trackDebug(type: String, extras: Map<String, Any>? = null) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        extras?.let { properties.putAll(extras) }
        analyticsProvider.get().trackEngineeringMetricEvents(DEBUG, properties)
    }

    fun trackFileUpload(step: String, remote_url: String, id: String) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = "FileUpload"
        properties["Step"] = step
        properties["remote_url"] = remote_url
        properties["id"] = id
        analyticsProvider.get().trackEngineeringMetricEvents(DEBUG, properties)
    }

    fun trackFileUploadError(step: String, reason: Throwable?) {
        val properties = mutableMapOf<String, Any>()
        properties["Step"] = step
        properties[REASON] = reason?.message ?: ""
        properties[CAUSE] = reason?.cause?.message ?: ""
        if (reason != null) properties[STACKTRACE] = reason.getStringStackTrace()
        analyticsProvider.get().trackEngineeringMetricEvents(FILE_UPLOAD_ERROR, properties)
    }

    fun trackFileUploadAwsUploaderChanges(status: String?, remote_url: String, id: String) {
        val properties = mutableMapOf<String, Any>()
        if (status != null) properties["Status"] = status
        properties["remote_url"] = remote_url
        properties["id"] = id
        analyticsProvider.get().trackEvents("File Upload: onStateChanged", properties)
    }

    fun trackViewLiveSales() {
        analyticsProvider.get().trackEvents(VIEW_LIVESALES, null)
    }

    fun trackStopLiveSale(timestamp: String) {
        val properties = mutableMapOf<String, Any>()
        properties[TIMESTAMP] = timestamp
        analyticsProvider.get().trackEvents(STOP_LIVESALE_QR, properties)
    }

    fun trackSubmitFeedback(
        value: String,
        setValue: Int,
        isFeedbackWritten: Boolean,
        screen: String,
        type: String,
        source: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[SET_VALUE] = setValue
        properties[VALUE] = value
        properties[FEEDBACK] = isFeedbackWritten
        properties[SCREEN] = screen
        properties[TYPE] = type
        if (!source.isNullOrBlank()) properties[SOURCE] = source
        analyticsProvider.get().trackEvents(SUBMIT_FEEDBACK, properties)
    }

    fun trackSelectRating(setValue: Int, screen: String, source: String? = null) {
        val properties = mutableMapOf<String, Any>()
        properties[SET_VALUE] = setValue
        properties[SCREEN] = screen
        if (!source.isNullOrBlank()) properties[SOURCE] = source
        analyticsProvider.get().trackEvents(SELECT_RATING, properties)
    }

    fun trackAddNoteClicked(
        flow: String,
        relation: String?,
        type: String?,
        method: String?,
        accountId: String?,
        txnId: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[FLOW] = flow
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!type.isNullOrBlank()) properties[TYPE] = type
        if (!method.isNullOrBlank()) properties[METHOD] = method
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        if (!txnId.isNullOrBlank()) properties[TXN_ID] = txnId
        analyticsProvider.get().trackEvents(ADD_NOTE_CLICKED, properties)
    }

    fun trackDeleteReceipt(
        flow: String?,
        relation: String?,
        type: String?,
        screen: String?,
        account: String?,
        txnId: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!type.isNullOrBlank()) properties[TYPE] = type
        if (!screen.isNullOrBlank()) properties[SCREEN] = screen
        if (!account.isNullOrBlank()) properties[ACCOUNT_ID] = account
        if (!txnId.isNullOrBlank()) properties[TXN_ID] = txnId
        analyticsProvider.get().trackEvents(DELETE_RECEIPT, properties)
    }

    fun trackAddNoteStarted(
        flow: String?,
        relation: String?,
        type: String?,
        method: String?,
        account: String?,
        txnId: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!type.isNullOrBlank()) properties[TYPE] = type
        if (!method.isNullOrBlank()) properties[METHOD] = method
        if (!account.isNullOrBlank()) properties[ACCOUNT_ID] = account
        if (!txnId.isNullOrBlank()) properties[TXN_ID] = txnId
        analyticsProvider.get().trackEvents(ADD_NOTE_STARTED, properties)
    }

    fun trackAddReceiptStarted(
        flow: String?,
        relation: String?,
        type: String?,
        screen: String?,
        account: String?,
        txnId: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!type.isNullOrBlank()) properties[TYPE] = type
        if (!screen.isNullOrBlank()) properties[SCREEN] = screen
        if (!account.isNullOrBlank()) properties[ACCOUNT_ID] = account
        if (!txnId.isNullOrBlank()) properties[TXN_ID] = txnId
        analyticsProvider.get().trackEvents(ADD_RECEIPT_STARTED, properties)
    }

    fun trackAddNoteCompleted(
        flow: String?,
        relation: String?,
        type: String?,
        method: String?,
        account: String?,
        txnId: String? = null,
        note: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!type.isNullOrBlank()) properties[TYPE] = type
        if (!method.isNullOrBlank()) properties[METHOD] = method
        if (!account.isNullOrBlank()) properties[ACCOUNT_ID] = account
        if (!txnId.isNullOrBlank()) properties[TXN_ID] = txnId
        if (!note.isNullOrBlank()) properties[PropertyKey.NOTE] = note
        analyticsProvider.get().trackEvents(ADD_NOTE_COMPLETED, properties)
    }

    fun trackEditReceipt(
        flow: String?,
        relation: String?,
        type: String?,
        account: String?,
        txnId: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!type.isNullOrBlank()) properties[TYPE] = type
        if (!account.isNullOrBlank()) properties[ACCOUNT_ID] = account
        if (!txnId.isNullOrBlank()) properties[TXN_ID] = txnId
        analyticsProvider.get().trackEvents(EDIT_RECEIPT, properties)
    }

    fun trackAddReceiptCompleted(
        flow: String?,
        relation: String?,
        type: String?,
        method: String,
        screen: String?,
        count: String,
        totalCount: String,
        account: String?,
        txnId: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!type.isNullOrBlank()) properties[TYPE] = type
        if (!method.isNullOrBlank()) properties[METHOD] = method
        if (!screen.isNullOrBlank()) properties[SCREEN] = screen
        if (!account.isNullOrBlank()) properties[ACCOUNT_ID] = account
        if (!count.isNullOrBlank()) properties[PropertyKey.COUNT] = count
        if (!totalCount.isNullOrBlank()) properties[TOTAL_COUNT] = totalCount
        if (!txnId.isNullOrBlank()) properties[TXN_ID] = txnId
        analyticsProvider.get().trackEvents(ADD_RECEIPT_COMPLETED, properties)
    }

    fun trackAppLockEnabled(type: String? = null, screen: String, flow: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[FLOW] = flow
        if (type.isNullOrBlank().not()) {
            properties[TYPE] = type!!
        }
        analyticsProvider.get().trackEvents(APP_LOCK_ENABLED, properties)
    }

    @NonNls
    fun trackAddCustomerSelectName(
        flow: String?,
        relation: String?,
        source: String? = null,
        defaultMode: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!source.isNullOrBlank()) properties[SOURCE] = source
        if (!defaultMode.isNullOrBlank()) properties[DEFAULT_MODE] = defaultMode
        analyticsProvider.get().trackEvents(SELECT_NAME, properties)
    }

    @NonNls
    fun trackAddCustomerConfirmName(
        flow: String?,
        relation: String?,
        source: String? = null,
        defaultMode: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!source.isNullOrBlank()) properties[SOURCE] = source
        if (!defaultMode.isNullOrBlank()) properties[DEFAULT_MODE] = defaultMode
        analyticsProvider.get().trackEvents(CONFIRM_NAME, properties)
    }

    @NonNls
    fun trackAddCustomerSelectMobile(
        flow: String?,
        relation: String?,
        source: String? = null,
        defaultMode: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!source.isNullOrBlank()) properties[SOURCE] = source
        if (!defaultMode.isNullOrBlank()) properties[DEFAULT_MODE] = defaultMode
        analyticsProvider.get().trackEvents(SELECT_MOBILE, properties)
    }

    @NonNls
    fun trackContactsPermissionPopUp(
        flow: String?,
        screen: String?,
        relation: String? = null,
        source: String? = null,
        defaultMode: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!screen.isNullOrBlank()) properties[SCREEN] = screen
        if (!source.isNullOrBlank()) properties[SOURCE] = source
        if (!defaultMode.isNullOrBlank()) properties[DEFAULT_MODE] = defaultMode
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        analyticsProvider.get().trackEvents(VIEW_CONTACT_PERMISSION, properties)
    }

    @NonNls
    fun trackImportContactClicked(
        flow: String?,
        screen: String?,
        relation: String? = null,
        source: String? = null,
        defaultMode: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!flow.isNullOrBlank()) properties[FLOW] = flow
        if (!screen.isNullOrBlank()) properties[SCREEN] = screen
        if (!source.isNullOrBlank()) properties[SOURCE] = source
        if (!defaultMode.isNullOrBlank()) properties[DEFAULT_MODE] = defaultMode
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        analyticsProvider.get().trackEvents(Event.IMPORT_CONTACT_CLICKED, properties)
    }

    fun trackAppLockDisabled(screen: String, flow: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[FLOW] = flow
        analyticsProvider.get().trackEvents(APP_LOCK_DISABLED, properties)
    }

    fun trackSecurityScreenAppLockEnableClick(type: String) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        analyticsProvider.get().trackEvents(SECURITY_SCREEN_APP_LOCK_ENABLE_CLICKCED, properties)
    }

    fun trackToDeviceLockSetting(type: String? = null, screen: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        if (type.isNullOrBlank().not()) {
            properties[TYPE] = type!!
        }
        analyticsProvider.get().trackEvents(DEVICE_LOCK_SETTING, properties)
    }

    fun trackShared(
        content: String?,
        platform: String?,
        packageId: String?,
        shareType: String?,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[CONTENT] = content ?: ""
        properties[PLATFORM] = platform ?: ""
        properties[PACKAGE_ID] = packageId ?: ""
        properties[SHARE_TYPE] = shareType ?: ""
        analyticsProvider.get().trackEvents(SHARED, properties)
    }

    fun trackSyncDirtyTransaction(step: String, localId: String, customerId: String = "", serverId: String = "") {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = "SyncTransactionsFile"
        properties["Step"] = step
        properties["Local Txn Id"] = localId
        if (customerId.isNotEmpty()) properties["Customer Id"] = customerId
        if (serverId.isNotEmpty()) properties["Server Txn Id"] = serverId
        analyticsProvider.get().trackEngineeringMetricEvents(SYNC_DIRTY_TRANSACTION, properties)
    }

    fun trackTransactionDetails(
        eventName: String,
        relation: String? = null,
        type: String,
        accountId: String? = null,
        status: String? = null,
        flow: String? = null,
        channel: String? = null,
        screen: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        properties[SCREEN] = TRANSACTION_DETAILS

        if (!screen.isNullOrBlank()) {
            properties[SCREEN] = screen
        }

        if (!relation.isNullOrBlank()) {
            properties[RELATION] = relation
        }
        if (!accountId.isNullOrBlank()) {
            properties[ACCOUNT_ID] = accountId
        }
        if (!status.isNullOrBlank()) {
            properties[STATUS] = status
        }
        if (!channel.isNullOrBlank()) {
            properties[CHANNEL] = channel
        }

        if (!flow.isNullOrBlank()) {
            properties[FLOW] = flow
        }
        analyticsProvider.get().trackEvents(eventName, properties)
    }

    fun trackSelectFeedback(accountId: String, relation: String) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[ACCOUNT_ID] = accountId
        analyticsProvider.get().trackEvents(SUBMIT_FEEBBACK, properties)
    }

    fun trackSubmitFeedback(accountId: String, relation: String) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[ACCOUNT_ID] = accountId
        analyticsProvider.get().trackEvents(SUBMIT_FEEBBACK, properties)
    }

    fun trackViewCommonLedger(relation: String, accountId: String, txPermission: Boolean) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[ACCOUNT_ID] = accountId
        properties[TX_PERMISSION] = txPermission
        analyticsProvider.get().trackEvents(VIEW_COMMON_LEDGER, properties)
    }

    fun trackViewRewards(screen: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.VIEW_REWARDS, properties)
    }

    fun trackSelectDueDate(
        screen: String,
        accountId: String?,
        relation: String,
        dueDate: Long?,
        dueRange: String?,
        flow: String,
        method: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        if (flow.isNotBlank()) properties[FLOW] = flow
        if (dueDate != null) properties[DUE_DATE] = dueDate
        if (!dueRange.isNullOrBlank()) properties[DUE_RANGE] = dueRange
        properties[RELATION] = relation
        properties[METHOD] = method
        analyticsProvider.get().trackEvents(Event.SELECT_DUE_DATE, properties)
    }

    fun trackDueDateConfirmed(
        screen: String,
        dueDate: Long,
        oldDueDate: Long?,
        accountID: String?,
        relation: String,
        flow: String,
        suggestedDaysSpan: String,
        dateType: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        if (!accountID.isNullOrBlank()) properties[ACCOUNT_ID] = accountID
        properties[DUE_DATE] = dueDate
        if (oldDueDate != null) properties[OLD_DUE_DATE] = oldDueDate
        if (suggestedDaysSpan.isNotBlank()) properties[SET_VALUE] = suggestedDaysSpan
        properties[RELATION] = relation
        properties[FLOW] = flow
        properties[DATE_TYPE] = dateType
        analyticsProvider.get().trackEvents(Event.DUE_DATE_CONFIRMED, properties)
    }

    fun trackClearDueDate(
        screen: String,
        accountID: String?,
        relation: String,
        flow: String,
        dateType: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        if (!accountID.isNullOrBlank()) properties[ACCOUNT_ID] = accountID
        properties[RELATION] = relation
        properties[FLOW] = flow
        properties[DATE_TYPE] = dateType
        analyticsProvider.get().trackEvents(Event.CLEAR_DUE_DATE, properties)
    }

    fun trackSelectFilter(relation: String, flow: String) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[FLOW] = flow
        analyticsProvider.get().trackEvents(Event.SELECT_FILTER, properties)
    }

    fun trackClearFilter(source: String, relation: String, flow: String) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        if (source.isNotEmpty()) properties[SCREEN] = source
        properties[FLOW] = flow
        analyticsProvider.get().trackEvents(Event.CLEAR_FILTER, properties)
    }

    fun trackUpdateFilter(relation: String, sortBy: String, dueRange: String, flow: String) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[SORT_BY] = sortBy
        properties[DUE_RANGE] = dueRange
        properties[FLOW] = flow
        analyticsProvider.get().trackEvents(Event.UPDATE_FILTER, properties)
    }

    fun trackCancelFilter(relation: String, flow: String, method: String) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[FLOW] = flow
        properties[METHOD] = method
        analyticsProvider.get().trackEvents(Event.CANCEL_FILTER, properties)
    }

    fun trackVoiceTransactionStarted(accountID: String?, relation: String) {
        val properties = mutableMapOf<String, Any>()
        if (!accountID.isNullOrBlank()) properties[ACCOUNT_ID] = accountID
        properties[RELATION] = relation
        analyticsProvider.get().trackEvents(Event.VOICE_TRANSACTION_STARTED, properties)
    }

    fun trackVoiceTransactionClosed(accountID: String?, relation: String) {
        val properties = mutableMapOf<String, Any>()
        if (!accountID.isNullOrBlank()) properties[ACCOUNT_ID] = accountID
        properties[RELATION] = relation
        analyticsProvider.get().trackEvents(Event.VOICE_TRANSACTION_CLOSED, properties)
    }

    fun trackVoiceTransactionCompleted(
        accountId: String?,
        relation: String,
        type: String,
        amount: Int?,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[RELATION] = relation
        properties[TYPE] = type
        if (amount != null) properties[AMOUNT] = amount.toString()
        analyticsProvider.get().trackEvents(Event.VOICE_TRANSACTION_COMPLETED, properties)
    }

    fun trackAppLockCardDisplayed(screen: String, type: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        analyticsProvider.get().trackEvents(Event.APPLOCK_CARD_DISPAYED, properties)
    }

    fun trackAppLockCardCancelled(screen: String, type: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        analyticsProvider.get().trackEvents(Event.APPLOCK_CROSS_ICON_CLICKED, properties)
    }

    fun trackViewCalenderPermission(screen: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.VIEW_CALENDER_PERMISSION, properties)
    }

    fun trackGrantPermission(screen: String, type: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        analyticsProvider.get().trackEvents(GRANT_PERMISSION, properties)
    }

    fun trackDenyPermission(screen: String, type: String, always: Boolean) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        properties[ALWAYS] = always
        analyticsProvider.get().trackEvents(DENY_PERMISSION, properties)
    }

    fun trackBlockRelation(event: String, relation: String, accountID: String?) {
        val properties = mutableMapOf<String, Any>()
        if (!accountID.isNullOrBlank()) properties[ACCOUNT_ID] = accountID
        properties[RELATION] = relation
        analyticsProvider.get().trackEvents(event, properties)
    }

    fun trackUnBlockRelation(
        event: String,
        relation: String,
        accountID: String?,
        screen: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!accountID.isNullOrBlank()) properties[ACCOUNT_ID] = accountID
        properties[RELATION] = relation
        if (screen != null) properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(event, properties)
    }

    fun trackOnGiveDiscountClicked(
        screen: String,
        relation: String,
        accountId: String?,
        type: String,
        dueDate: Long? = null,
        dueRange: String? = null,
        dueAmount: Long = -1,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        properties[RELATION] = relation
        properties[SCREEN] = screen
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        if (dueDate != null) properties[DUE_DATE] = dueDate
        if (!dueRange.isNullOrBlank()) properties[DUE_RANGE] = dueRange
        properties[DUE_AMOUNT] = dueAmount
        analyticsProvider.get().trackEvents(Event.ADD_DISCOUNT_STARTED, properties)
    }

    fun trackAddDiscountCompleted(
        screen: String,
        relation: String,
        accountId: String?,
        type: String,
        amount: Long,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        properties[RELATION] = relation
        properties[SCREEN] = screen
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[AMOUNT] = amount
        analyticsProvider.get().trackEvents(Event.ADD_DISCOUNT_COMPLETED, properties)
    }

    fun trackViewMore(screen: String, relation: String, accountId: String?) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[SCREEN] = screen
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        analyticsProvider.get().trackEvents(Event.VIEW_MORE_CLICK, properties)
    }

    fun trackRelationShipMigrationStarted(
        relation: String,
        migrationRelation: String,
        screen: String,
        accountId: String?,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[MIGRATING_RELATION] = migrationRelation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.RELATIONSHIP_MIGRATION_STARTED, properties)
    }

    fun trackAllowRelationShipMigration(
        relation: String,
        migratingRelation: String,
        screen: String,
        accountId: String?,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[MIGRATING_RELATION] = migratingRelation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.ALLOW_RELATIONSHIP_MIGRATION, properties)
    }

    fun trackConfirmRelationShipMigration(
        relation: String,
        migratingRelation: String,
        screen: String,
        accountId: String?,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[MIGRATING_RELATION] = migratingRelation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.CONFIRM_RELATIONSHIP_MIGRATION, properties)
    }

    fun trackCancelRelationShipMigration(
        relation: String,
        migratingRelation: String,
        screen: String,
        accountId: String?,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[MIGRATING_RELATION] = migratingRelation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.CANCEL_RELATIONSHIP_MIGRATION, properties)
    }

    fun trackRelationMigrationSuccess(
        relation: String,
        migratingRelation: String,
        screen: String,
        accountId: String?,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[MIGRATING_RELATION] = migratingRelation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.RELATION_MIGRATION_SUCCESS, properties)
    }

    fun trackRelationMigrationFailed(
        relation: String,
        migratingRelation: String,
        screen: String,
        accountId: String?,
        errorMessage: String?,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[MIGRATING_RELATION] = migratingRelation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        if (!errorMessage.isNullOrBlank()) properties[REASON] = errorMessage
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.RELATION_MIGRATION_FAILED, properties)
    }

    fun trackRetryMigration(
        relation: String,
        migratingRelation: String,
        screen: String,
        accountId: String?,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[MIGRATING_RELATION] = migratingRelation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[SCREEN] = screen
        analyticsProvider.get().trackEvents(Event.RETRY_MIGRATION, properties)
    }

    fun trackAddRelationshipFailed(relation: String, reason: String) {
        val properties = mutableMapOf<String, Any>()
        properties[RELATION] = relation
        properties[REASON] = reason
        analyticsProvider.get().trackEvents(Event.ADD_RELATIONSHIP_FAILED, properties)
    }

    fun trackCancelDueDate(
        screen: String,
        accountId: String?,
        relation: String,
        flow: String,
        method: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[RELATION] = relation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[FLOW] = flow
        properties[METHOD] = method
        analyticsProvider.get().trackEvents(Event.CANCEL_DUE_DATE, properties)
    }

    fun trackVideoEvents(type: String, screen: String, playerType: String) {
        val properties = mutableMapOf<String, Any>()
        properties[SCREEN] = screen
        properties[TYPE] = type
        properties[VIDEO_TYPE] = playerType
        analyticsProvider.get().trackEvents(Event.YOUTUBE_VIDEO, properties)
    }

    fun trackSyncTxnsFcmNotificationReceived(transactionId: String) {
        val properties = mutableMapOf<String, Any>()
        properties[TXN_ID] = transactionId
        analyticsProvider.get().trackEvents(Event.FCM_NOTIFICATION_RECEIVED, properties)
    }

    fun trackSyncTxnsSuccessful(transactionId: String) {
        val properties = mutableMapOf<String, Any>()
        properties[TXN_ID] = transactionId
        analyticsProvider.get().trackEvents(Event.SYNC_TRANSACTIONS_SUCCESSFUL, properties)
    }

    fun trackViewHelpItem_v3(
        type: String,
        screen: String,
        source: String?,
        interaction: String,
        method: String,
        accountId: String?,
        relation: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[TYPE] = type
        properties[SCREEN] = screen
        if (source != null) properties[SOURCE] = source
        properties[INTERACTION] = interaction
        properties[METHOD] = method
        properties[RELATION] = relation
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        analyticsProvider.get().trackEvents(VIEW_HELP_ITEM, properties)
    }

    fun trackInAppDisplayedSMS(event: String, service: String, merchantId: String, source: String) {
        val properties = mutableMapOf<String, Any>()
        properties[MERCHANT_ID] = merchantId
        properties[SOURCE] = source
        properties[SERVICE] = service
        analyticsProvider.get().trackEvents(event, properties)
    }

    fun trackSupplierLearnMore(
        accountId: String?,
        relation: String,
        screen: String,
        listStatus: String,
        position: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[RELATION] = relation
        properties[SCREEN] = screen
        properties[SUPPLIER_LIST] = listStatus
        properties[POSITION] = position
        analyticsProvider.get().trackEvents(Event.CLICK_HELP_SECTION, properties)
    }

    fun trackChatIconClicked(
        accountId: String?,
        relation: String,
        unreadMessageCount: String,
        screen: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[RELATION] = relation
        properties[SCREEN] = screen
        properties[UNREAD_COUNT] = unreadMessageCount
        analyticsProvider.get().trackEvents(Event.CHAT_ICON_CLICKED, properties)
    }

    fun trackBillIconClicked(
        accountId: String?,
        relation: String,
        unreadBillCount: Int,
        totalBillCount: Int,
        screen: String,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[RELATION] = relation
        properties[SCREEN] = screen
        properties[PropertyKey.COUNT] = totalBillCount
        properties[UNREAD_COUNT] = unreadBillCount
        properties["Notification count"] = unreadBillCount
        properties["Position"] = "Top"
        analyticsProvider.get().trackEvents(Event.BILL_ICON_CLICKED, properties)
    }

    fun trackPageViewed(accountId: String) {
        val properties = mutableMapOf<String, Any>()
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        properties[RELATION] = SUPPLIER
        properties[SCREEN] = RELATIONSHIP
        analyticsProvider.get().trackEvents(Event.PAGE_VIEWED, properties)
    }

    fun trackKycBannerShown(type: String, screen: String, bannerType: String) {
        val properties = mapOf(TYPE to type, SCREEN to screen, "bannerType" to bannerType)
        analyticsProvider.get().trackEvents(KYC_BANNER_SHOWN, properties)
    }

    fun trackStartKycClicked(type: String, screen: String) {
        val properties = mapOf(TYPE to type, SCREEN to screen)
        analyticsProvider.get().trackEvents(START_KYC_CLICKED, properties)
    }

    fun trackPayOnlinePageView(
        accountId: String?,
        screen: String,
        type: String,
        relation: String,
    ) {
        val properties = mutableMapOf(
            SCREEN to screen,
            TYPE to type,
            RELATION to relation,
        )
        if (!accountId.isNullOrBlank()) properties[ACCOUNT_ID] = accountId
        analyticsProvider.get().trackEvents(PAY_ONLINE_PAGE_VIEW, properties)
    }

    fun trackPayOnlineCashbackPageView(
        accountId: String,
        screen: String,
        type: String,
        relation: String,
        cashbackMessageType: String,
        cashbackAmount: String,
        minimumPaymentAmount: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            SCREEN to screen,
            TYPE to type,
            RELATION to relation,
            CASHBACK_MESSAGE_TYPE to cashbackMessageType,
            CASHBACK_AMOUNT to cashbackAmount,
            MINIMUM_PAYMENT_AMOUNT to minimumPaymentAmount
        )
        analyticsProvider.get().trackEvents(PAY_ONLINE_CASHBACK_PAGE_VIEW, properties)
    }

    fun trackEnterAmountCashbackPageView(
        accountId: String,
        screen: String,
        type: String,
        relation: String,
        cashbackMessageType: String,
        cashbackAmount: String,
        minimumPaymentAmount: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            SCREEN to screen,
            TYPE to type,
            RELATION to relation,
            CASHBACK_MESSAGE_TYPE to cashbackMessageType,
            CASHBACK_AMOUNT to cashbackAmount,
            MINIMUM_PAYMENT_AMOUNT to minimumPaymentAmount
        )
        analyticsProvider.get().trackEvents(ENTER_AMOUNT_CASHBACK_PAGE_VIEW, properties)
    }

    fun trackKycDialogPopup(
        type: String,
        source: String,
    ) {
        val properties = mapOf(
            TYPE to type,
            SOURCE to source,
        )
        analyticsProvider.get().trackEvents(KYC_DIALOG_POPUP, properties)
    }

    fun trackEntryPointViewed(
        source: String,
        type: String,
        name: String? = null,
        value: String? = null,
        target: String = "",
        relation: String? = null,
        kycMessageType: String? = null,
    ) {
        val properties = mutableMapOf(
            SOURCE to source,
            TYPE to type,
            "Target" to target
        )
        if (!name.isNullOrEmpty()) properties[NAME] = name
        if (!value.isNullOrEmpty()) properties[VALUE] = value
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!kycMessageType.isNullOrBlank()) properties[KYC_MESSAGE_TYPE] = kycMessageType

        analyticsProvider.get().trackEvents(ENTRY_POINT_VIEWED, properties)
    }

    fun trackEntryPointClicked(
        source: String,
        type: String,
        relation: String? = null,
        kycMessageType: String? = null,
        riskType: String? = null,
        dailyLimitLeft: Long? = null,
        target: String? = null
    ) {
        val properties = mutableMapOf<String, Any>(
            SOURCE to source,
            TYPE to type,
        )

        if (!target.isNullOrBlank()) properties[TARGET] = target
        if (!relation.isNullOrBlank()) properties[RELATION] = relation
        if (!riskType.isNullOrBlank()) properties[RISK_TYPE] = riskType
        if (!kycMessageType.isNullOrBlank()) properties[KYC_MESSAGE_TYPE] = kycMessageType
        if (dailyLimitLeft != null) properties[DAILY_LIMIT_LEFT] = dailyLimitLeft

        analyticsProvider.get().trackEvents(ENTRY_POINT_CLICKED, properties)
    }

    fun trackKycEntryPointDismissed(
        riskType: String,
        screen: String,
        relation: String,
        dailyLimitLeft: Long,
    ) {
        val properties = mapOf(
            RISK_TYPE to riskType,
            SCREEN to screen,
            RELATION to relation,
            DAILY_LIMIT_LEFT to dailyLimitLeft,
        )
        analyticsProvider.get().trackEvents(KYC_ENTRY_POINT_DISMISSED, properties)
    }

    fun trackTransactionInfo(
        eventName: String,
        amount: String,
        billDate: String,
        txnId: String,
        screen: String,
        customerId: String,
        error: String,
    ) {
        val properties =
            mapOf(
                AMOUNT to amount,
                BillDate to billDate,
                LOCAL_TXN_ID to txnId,
                SCREEN to screen,
                CUSTOMER_ID to customerId,
                ERROR to error
            )
        analyticsProvider.get().trackEvents(eventName, properties)
    }

    fun trackSyncSupplierTxnsSuccessful(transactionId: String?) {
        val properties = mutableMapOf<String, Any>()
        properties[TXN_ID] = transactionId ?: ""
        analyticsProvider.get().trackEvents(Event.SUPPLIER_SYNC_TXN_SUCCESSFUL, properties)
    }

    fun trackCallPermissionGranted(source: String) {
        val properties = mapOf(
            Property.SOURCE to source,
            Property.PERMISSION to "Call"
        )
        analyticsProvider.get().trackEvents(PERMISSION_GRANTED, properties)
    }

    fun trackCallPermissionDenied(source: String) {
        val properties = mapOf(
            Property.SOURCE to source,
            Property.PERMISSION to "Call"
        )
        analyticsProvider.get().trackEvents(PERMISSION_DENIED, properties)
    }
}
