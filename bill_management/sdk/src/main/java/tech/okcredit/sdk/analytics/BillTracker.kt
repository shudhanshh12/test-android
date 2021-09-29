package tech.okcredit.sdk.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey
import dagger.Lazy
import org.joda.time.DateTime
import tech.okcredit.BillGlobalInfo
import tech.okcredit.FilterRange
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.sdk.models.SelectedDateMode
import tech.okcredit.sdk.store.database.LocalBill
import javax.inject.Inject

class BillTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    object Key {
        const val STEP = "step"
        const val ID = "id"
        const val SOURCE = "source"
        const val IS_FILE = "Is File"
        const val REASON = "Reason"
        const val STACKTRACE = "StackTrace"
        const val SCREEN = "Screen"
        const val RELATION = "Relation"
        const val TYPE = "Type"
        const val META = "Meta"
    }

    object Event {
        const val SYNC_BILLS = "BillSdk Sync Bills"
        const val SYNC_BILLS_ERROR = "BillSdk Sync Bills: Error"
        const val DEBUG = "Debug"
    }

    object DebugType {
        const val FILE_DOWNLOAD_STATE = "File Download State"
    }

    fun trackSyncBills(step: String, flowId: String, source: String, type: String = "", count: Int = 0) {
        val properties = HashMap<String, String>().apply {
            this[Key.STEP] = step
            this[Key.ID] = flowId
            this[Key.SOURCE] = source
            this[Key.TYPE] = type
            this[PropertyKey.COUNT] = count.toString()
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.SYNC_BILLS, properties)
    }

    fun trackSyncTransactionError(isFile: Boolean, type: String, flowId: String, reason: String?, stackTrace: String?) {
        val properties = HashMap<String, String>().apply {
            this[Key.IS_FILE] = isFile.toString()
            this[Key.TYPE] = type
            this[Key.ID] = flowId
            this[Key.REASON] = reason ?: ""
            this[Key.STACKTRACE] = stackTrace ?: ""
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.SYNC_BILLS_ERROR, properties)
    }

    fun trackDebug(type: String, meta: String) {
        val properties = HashMap<String, String>().apply {
            this[Key.TYPE] = type
            this[Key.META] = meta
        }
        analyticsProvider.get().trackEngineeringMetricEvents(Event.DEBUG, properties)
    }

    fun trackEvents(
        eventName: String,
        type: String? = null,
        screen: String? = null,
        relation: String? = null,
        source: String? = null,
        value: Boolean? = null,
        focalArea: Boolean? = null,
        eventProperties: HashMap<String, Any>? = null
    ) {
        var properties = eventProperties
        if (properties != null) {
            properties.apply {
                addProperties(screen, type, relation, value, source, focalArea)
            }
        } else {
            properties = HashMap<String, Any>().apply {
                addProperties(screen, type, relation, value, source, focalArea)
            }
        }

        analyticsProvider.get().trackEvents(eventName, properties)
    }

    private fun HashMap<String, Any>.addProperties(
        screen: String?,
        type: String?,
        relation: String?,
        value: Boolean?,
        source: String?,
        focalArea: Boolean?
    ) {
        screen?.let {
            this[PropertyKey.SCREEN] = screen
        }

        type?.let {
            this[PropertyKey.TYPE] = type
        }

        relation?.let {
            this[PropertyKey.RELATION] = relation
        }

        value?.let {
            this[PropertyKey.VALUE] = value
        }

        source?.let {
            this[PropertyKey.SOURCE] = source
        }

        focalArea?.let {
            this[PropertyKey.FOCAL_AREA] = focalArea
        }
    }

    fun trackRuntimePermission(screen: String, type: String, granted: Boolean) {
        if (granted)
            trackEvents(`in`.okcredit.analytics.Event.GRANT_PERMISSION, screen = screen, type = type)
        else
            trackEvents(`in`.okcredit.analytics.Event.DENY_PERMISSION, screen = screen, type = type)
    }

    fun trackPopUpDisplayed(screen: String, type: String) {
        val eventProperties = HashMap<String, Any>().apply {

            this[PropertyKey.RELATION] = BillGlobalInfo.relation
            this[PropertyKey.SCREEN] = screen
            this[PropertyKey.TYPE] = type
            this[PropertyKey.ACCOUNT_ID] = BillGlobalInfo.accountId
        }

        trackEvents("Popup Displayed", eventProperties = eventProperties)
    }

    fun trackPopupClicked(screen: String, type: String) {
        val eventProperties = HashMap<String, Any>().apply {

            this[PropertyKey.RELATION] = BillGlobalInfo.relation
            this[PropertyKey.SCREEN] = screen
            this[PropertyKey.TYPE] = type
            this[PropertyKey.ACCOUNT_ID] = BillGlobalInfo.accountId
        }

        trackEvents("Popup Clicked", eventProperties = eventProperties)
    }

    fun trackPopupScrolled(position: Int) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties["Position"] = position
        trackEvents("Popup Scrolled", eventProperties = eventProperties)
    }

    private fun addBillSuperProperties(eventProperties: java.util.HashMap<String, Any>) {
        eventProperties.apply {
            this[PropertyKey.RELATION] = BillGlobalInfo.relation
            this[PropertyKey.ACCOUNT_ID] = BillGlobalInfo.accountId
            this[PropertyKey.VALUE] = BillGlobalInfo.value
            this[PropertyKey.DATE_RANGE] = BillGlobalInfo.dateRange
        }
    }

    fun trackPageViewed() {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties[PropertyKey.COUNT] = BillGlobalInfo.totalAccountBills
        eventProperties[PropertyKey.UNREAD_COUNT] = BillGlobalInfo.unseenAccountBills
        trackEvents("Page Viewed", eventProperties = eventProperties)
    }

    fun trackAddBillClicked(source: String, billCount: Int, billId: String?) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)

        eventProperties.apply {
            this["Source"] = source
            this[PropertyKey.COUNT] = billCount
            if (billId != null)
                this["Bill Id"] = billId
        }
        trackEvents("Click Add Bill Confirm", eventProperties = eventProperties)
    }

    fun trackAddBillSuccess(
        size: Int?,
        date: DateTime?,
        note: String?,
        billId: String?,
        defaultDateChange: Boolean,
        label: String,
        flow: String,
    ) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            if (size != null)
                this[PropertyKey.COUNT] = size
            if (date != null)
                this["Bill Date"] = DateTimeUtils.formatDateOnly(date)
            if (note != null)
                this["Transaction Notes"] = note
            if (billId != null)
                this["Bill Id"] = billId
            this["Default date Changed"] = defaultDateChange
            this["Label"] = label
            this["Flow"] = flow
        }
        trackEvents("Add Bill Success", eventProperties = eventProperties)
    }

    fun trackChooseImage(source: String, billCount: Int, billId: String?) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            this["Source"] = source
            this[PropertyKey.COUNT] = billCount
            if (billId != null)
                this["Bill Id"] = billId
        }
        trackEvents("Choose Image", eventProperties = eventProperties)
    }

    fun trackAddNoteClicked(
        flow: String,
        method: String?,
        type: String,
    ) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            this["Flow"] = flow
            if (method != null)
                this["Method"] = method
            this["Bill Type"] = type
        }
        trackEvents("Add Note Clicked", eventProperties = eventProperties)
    }

    fun trackAddNoteStarted(
        flow: String,
        method: String?,
        type: String
    ) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            this["Flow"] = flow
            if (method != null)
                this["Method"] = method
            this["Bill Type"] = type
        }
        trackEvents("Add Note Started", eventProperties = eventProperties)
    }

    fun trackAddNoteCompleted(
        flow: String,
        method: String?,
        type: String,
        note: String
    ) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            this["Flow"] = flow
            if (method != null)
                this["Method"] = method
            this["Bill Type"] = type
            this["Content"] = note
        }
        trackEvents("Add Note Completed", eventProperties = eventProperties)
    }

    fun trackDateClicked() {
        val eventProperties = HashMap<String, Any>().apply {
            this["Bill Type"] = "Bill Management"
        }
        addBillSuperProperties(eventProperties)
        trackEvents("Date Clicked", eventProperties = eventProperties)
    }

    fun trackDateUpdate(
        screen: String,
        from: Long,
        to: Long
    ) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)

        val formattedDateUpdateTo = DateTimeUtils.formatDateOnly(DateTime(to))
        val formattedDateUpdateFrom = DateTimeUtils.formatDateOnly(DateTime(from))

        eventProperties.apply {
            this["date updated To"] = formattedDateUpdateTo
            this["date updated From"] = formattedDateUpdateFrom
            this["Date Update"] = screen
        }
        trackEvents("Date Update", eventProperties = eventProperties)
    }

    fun trackBillViewed(localBill: LocalBill, billCount: Int) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            val millis = localBill.billDate?.toLongOrNull()
            if (millis != null)
                this["Bill Date"] = DateTimeUtils.formatDateOnly(DateTime(millis))
            if (localBill.note != null)
                this["Transaction Notes"] = localBill.note
            this["Bill Id"] = localBill.id
            this["Bill Type"] = if (localBill.transactionId.isNullOrEmpty()) "Individual" else "Transactional"
            if (localBill.txnType != null)
                this["Transaction Type"] = localBill.txnType.type
            if (localBill.amount != null)
                this["Transaction Amount"] = localBill.amount
            this["Owner"] = localBill.createdByMe
            if (localBill.transactionId != null)
                this["tx_id"] = localBill.transactionId
        }
        eventProperties[PropertyKey.COUNT] = billCount
        eventProperties[PropertyKey.UNREAD_COUNT] = BillGlobalInfo.unseenAccountBills
        trackEvents("View Bill", eventProperties = eventProperties)
    }

    fun trackBillDeleteClicked(localBill: LocalBill) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            val millis = localBill.billDate?.toLongOrNull()
            if (millis != null)
                this["Bill Date"] = DateTimeUtils.formatDateOnly(DateTime(millis))
            if (localBill.note != null)
                this["Transaction Notes"] = localBill.note
            this["Bill Id"] = localBill.id
            this["Bill Type"] = if (localBill.transactionId.isNullOrEmpty()) "Individual" else "Transactional"
            if (localBill.txnType != null)
                this["Transaction Type"] = localBill.txnType.type
            if (localBill.amount != null)
                this["Transaction Amount"] = localBill.amount
            this["Label"] = localBill.status
            this["Owner"] = localBill.createdByMe
            if (localBill.transactionId != null)
                this["tx_id"] = localBill.transactionId
        }
        trackEvents("Delete Bill", eventProperties = eventProperties)
    }

    fun trackDownloadBillClicked(localBill: LocalBill) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            val millis = localBill.billDate?.toLongOrNull()
            if (millis != null)
                this["Bill Date"] = DateTimeUtils.formatDateOnly(DateTime(millis))
            if (localBill.note != null)
                this["Transaction Notes"] = localBill.note
            this["Bill Id"] = localBill.id
            this["Bill Type"] = if (localBill.transactionId != null) "Transactional" else "Individual"
            if (localBill.txnType != null)
                this["Transaction Type"] = localBill.txnType.type
            if (localBill.amount != null)
                this["Transaction Amount"] = localBill.amount
            this["Label"] = localBill.status
            this["Owner"] = localBill.createdByMe
            if (localBill.transactionId != null)
                this["tx_id"] = localBill.transactionId
        }
        trackEvents("Download Bill Clicked", eventProperties = eventProperties)
    }

    fun trackBillDownloaded(localBill: LocalBill) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            val millis = localBill.billDate?.toLongOrNull()
            if (millis != null)
                this["Bill Date"] = DateTimeUtils.formatDateOnly(DateTime(millis))
            if (localBill.note != null)
                this["Transaction Notes"] = localBill.note
            this["Bill Id"] = localBill.id
            this["Bill Type"] = if (localBill.transactionId.isNullOrEmpty()) "Individual" else "Transactional"
            if (localBill.txnType != null)
                this["Transaction Type"] = localBill.txnType.type
            if (localBill.amount != null)
                this["Transaction Amount"] = localBill.amount
            this["Label"] = localBill.status
            this["Owner"] = localBill.createdByMe
            if (localBill.transactionId != null)
                this["tx_id"] = localBill.transactionId
        }
        trackEvents("Bill Downloaded", eventProperties = eventProperties)
    }

    fun trackDateRangeClick() {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            this["Type"] = "Bill Management"
        }
        eventProperties[PropertyKey.COUNT] = BillGlobalInfo.totalAccountBills
        eventProperties[PropertyKey.UNREAD_COUNT] = BillGlobalInfo.unseenAccountBills
        trackEvents("bill_gallery_date_click", eventProperties = eventProperties)
    }

    fun trackFilterUpdate(startTime: DateTime?, endTime: DateTime, selectMode: SelectedDateMode) {
        val eventProperties = HashMap<String, Any>()
        val formattedStartTime = if (startTime != null) DateTimeUtils.formatDateOnly(startTime) else "0"
        val formattedEndTime = DateTimeUtils.formatDateOnly(endTime)
        BillGlobalInfo.dateRange = mutableListOf<String>().apply {
            add(formattedStartTime)
            add(formattedEndTime)
        }

        BillGlobalInfo.value = when (selectMode) {
            SelectedDateMode.CUSTOM_DATE -> FilterRange.DATE_RANGE
            SelectedDateMode.LAST_MONTH -> FilterRange.LAST_MONTH
            SelectedDateMode.OVERALL -> FilterRange.ALL
            SelectedDateMode.CURRENT -> FilterRange.THIS_MONTH
            SelectedDateMode.LAST_TO_LAST -> FilterRange.LAST_TO_LAST_MONTH
        }
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            this["Type"] = "Bill Management"
        }
        eventProperties[PropertyKey.COUNT] = BillGlobalInfo.totalAccountBills
        eventProperties[PropertyKey.UNREAD_COUNT] = BillGlobalInfo.unseenAccountBills
        trackEvents("bill_gallery_date_filter_update", eventProperties = eventProperties)
    }

    fun trackAddReceiptStarted(
        flow: String,
        billId: String? = null,
        transactionId: String? = null,
        count: Int? = null
    ) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            this["Flow"] = flow
            billId?.let { this["Bill Id"] = it }
            count?.let { this["count"] = it }
            if (!transactionId.isNullOrBlank()) {
                this["Tx_id"] = transactionId
                this["Bill Type"] = "Transaction"
            } else {
                this["Bill Type"] = "Bill Management"
            }
        }
        trackEvents("Add Receipt Started Bill management", eventProperties = eventProperties)
    }

    fun trackAddMoreBillManagement(flow: String, billId: String? = null) {
        val eventProperties = HashMap<String, Any>()
        addBillSuperProperties(eventProperties)
        eventProperties.apply {
            this["Flow"] = flow
            if (billId != null)
                this["Bill Id"] = billId
        }
        trackEvents("Add More Image Bill management", eventProperties = eventProperties)
    }
}
