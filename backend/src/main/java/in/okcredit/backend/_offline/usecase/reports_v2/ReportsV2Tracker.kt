package `in`.okcredit.backend._offline.usecase.reports_v2

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyValue.CAUSE
import `in`.okcredit.analytics.PropertyValue.REASON
import `in`.okcredit.analytics.PropertyValue.STACKTRACE
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport.ReportType.*
import `in`.okcredit.backend._offline.usecase.reports_v2.ReportsV2Tracker.Event.ACC_REPORT_DOWNLOAD_SUCCESSFUL
import `in`.okcredit.backend._offline.usecase.reports_v2.ReportsV2Tracker.Key.REPORT_TYPE_SC
import dagger.Lazy
import tech.okcredit.android.base.utils.getStringStackTrace
import javax.inject.Inject

class ReportsV2Tracker @Inject constructor(
    private val tracker: Lazy<AnalyticsProvider>,
) {
    object Key {
        const val POLL_COUNT = "Poll Count"
        const val REPORT_TYPE = "Report Type"
        const val ACCOUNT_ID = "Account Id"
        const val START_TIME = "Start Time"
        const val END_TIME = "End Time"
        const val RELATION = "Relation"
        const val SUCCESS = "success"
        const val RESPONSE = "response"
        const val ACCOUNT_ID_SC = "account_id"
        const val REPORT_TYPE_SC = "report_type"
        const val POLL_COUNT_SC = "poll_count"
        const val START_TIME_SC = "start_time"
        const val END_TIME_SC = "end_time"
    }

    object PropertyValue {
        const val CUSTOMER_REPORT = "Customer Report"
        const val SUPPLIER_REPORT = "Supplier Report"
        const val SUCCESSS = "Success"
        const val NETWORK_ERROR = "Network Error"
        const val UNKNOWN_ERROR = "Unknown"
    }

    object Event {
        const val DOWNLOAD_REPORT_FAILED = "Download Report V2 Failed"
        const val DOWNLOAD_REPORT_STARTED = "Download Report V2 Started"
        const val REPORT_CREATED = "report_created"
        const val ACC_REPORT_DOWNLOAD_SUCCESSFUL = "acct_report_download_successful"
    }

    fun trackError(
        exception: Throwable?,
        pollCount: Int,
        reportTypeName: String,
        accountId: String?,
        startTimeSec: Long,
        endTimeSec: Long,
    ) {
        val properties = mutableMapOf<String, Any>(
            Key.POLL_COUNT to pollCount,
            Key.REPORT_TYPE to reportTypeName,
            REASON to (exception?.message ?: ""),
            CAUSE to (exception?.cause?.message ?: ""),
            Key.ACCOUNT_ID to (accountId ?: ""),
            Key.START_TIME to startTimeSec,
            Key.END_TIME to endTimeSec
        )
        if (exception != null) properties[STACKTRACE] = exception.getStringStackTrace()
        tracker.get().trackEvents(Event.DOWNLOAD_REPORT_FAILED, properties)
    }

    fun trackSuccess(reportTypeName: String, pollCount: Int, accountId: String?, startTimeSec: Long, endTimeSec: Long) {
        val eventName = getEventNameForReportType(reportTypeName)

        if (eventName == ACC_REPORT_DOWNLOAD_SUCCESSFUL) {
            trackAccountReportDownloadSuccess(
                reportTypeName,
                pollCount, accountId,
                startTimeSec,
                endTimeSec
            )
        } else {
            val properties = mutableMapOf<String, Any>(
                Key.POLL_COUNT to pollCount,
                Key.ACCOUNT_ID to (accountId ?: ""),
                Key.START_TIME to startTimeSec,
                Key.END_TIME to endTimeSec,
            )

            tracker.get().trackEvents(eventName, properties)
        }
    }

    private fun getEventNameForReportType(reportTypeName: String): String {
        return when (valueOf(reportTypeName)) {
            BACKUP_ALL -> "Download Backup Successful"
            CUSTOMER_ACCOUNT -> "Download Account Statement Successful"
            SUPPLIER_ACCOUNT -> "Download Supplier Account Statement Successful"
            CUSTOMER_REPORT,
            SUPPLIER_REPORT,
            -> ACC_REPORT_DOWNLOAD_SUCCESSFUL
        }
    }

    fun trackWorkerStarted(reportTypeName: String, accountId: String?, startTimeSec: Long, endTimeSec: Long) {
        val properties = mutableMapOf<String, Any>(
            Key.REPORT_TYPE to reportTypeName,
            Key.ACCOUNT_ID to (accountId ?: ""),
            Key.START_TIME to startTimeSec,
            Key.END_TIME to endTimeSec
        )
        tracker.get().trackEvents(Event.DOWNLOAD_REPORT_STARTED, properties)
    }

    fun trackReportCreated(
        reportTypeName: String,
        success: Boolean,
        accountId: String,
        response: String,
    ) {

        val properties = mutableMapOf(
            REPORT_TYPE_SC to reportTypeName,
            Key.ACCOUNT_ID_SC to accountId,
            Key.SUCCESS to success,
            Key.RESPONSE to response,
        )

        tracker.get().trackEvents(Event.REPORT_CREATED, properties)
    }

    fun trackAccountReportDownloadSuccess(
        reportTypeName: String,
        pollCount: Int,
        accountId: String?,
        startTimeSec: Long,
        endTimeSec: Long,
    ) {
        val reportType = if (valueOf(reportTypeName) == CUSTOMER_REPORT) {
            PropertyValue.CUSTOMER_REPORT
        } else {
            PropertyValue.SUPPLIER_REPORT
        }
        val properties = mutableMapOf<String, Any>(
            Key.POLL_COUNT_SC to pollCount,
            Key.ACCOUNT_ID_SC to (accountId ?: ""),
            Key.START_TIME_SC to startTimeSec,
            Key.END_TIME_SC to endTimeSec,
            REPORT_TYPE_SC to reportType,
        )
        tracker.get().trackEvents(ACC_REPORT_DOWNLOAD_SUCCESSFUL, properties)
    }
}
