package `in`.okcredit.backend._offline.usecase.reports_v2

import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport.ReportType.*
import `in`.okcredit.shared.utils.CommonUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class DownloadReportFileNameProvider @Inject constructor() {

    companion object {
        private const val FILE_NAME_DATE_FORMAT = "dd-MM-yyyy"
    }

    fun execute(request: DownloadReport.Request): String {
        return when (request.reportType) {
            BACKUP_ALL -> addCurrentDate(request.reportType.fileName)
            CUSTOMER_ACCOUNT, SUPPLIER_ACCOUNT,
            CUSTOMER_REPORT, SUPPLIER_REPORT -> addStartAndEndDate(request.reportType.fileName, request)
        }
    }

    private fun addCurrentDate(fileName: String): String {
        val dayStartTime = CommonUtils.currentDateTime()
        return String.format(fileName, dayStartTime.toDateString())
    }

    private fun addStartAndEndDate(fileName: String, request: DownloadReport.Request): String {
        val startDate = request.startTimeSec?.toDateString() ?: ""
        val endDate = request.endTimeSec?.toDateString() ?: ""
        return String.format(fileName, startDate, endDate)
    }

    private fun DateTime.toDateString() = this.toString(DateTimeFormat.forPattern(FILE_NAME_DATE_FORMAT))
}
