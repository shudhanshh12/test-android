package `in`.okcredit.backend._offline.usecase.reports_v2

import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadReportFileNameProviderTest {

    private val downloadReportFileNameProvider = DownloadReportFileNameProvider()

    @Test
    fun `execute() given report type CUSTOMER_ACCOUNT should return expected file name`() {
        // Given
        val reportType = DownloadReport.ReportType.CUSTOMER_ACCOUNT
        val startTime = DateTime(1604590843000)
        val endTime = DateTime(1607182945000)

        // When
        val fileName = downloadReportFileNameProvider.execute(
            DownloadReport.Request(
                reportType = reportType,
                startTimeSec = startTime,
                endTimeSec = endTime,
                workName = "worker-1",
            )
        )

        // Then
        assertEquals("OkCredit_AccountStatement_05-11-2020_05-12-2020.pdf", fileName)
    }

    @Test
    fun `execute() given report type SUPPLIER_ACCOUNT should return expected file name`() {
        // Given
        val reportType = DownloadReport.ReportType.SUPPLIER_ACCOUNT
        val startTime = DateTime(1604590843000)
        val endTime = DateTime(1607182945000)

        // When
        val fileName = downloadReportFileNameProvider.execute(
            DownloadReport.Request(
                reportType = reportType,
                startTimeSec = startTime,
                endTimeSec = endTime,
                workName = "worker-1",
            )
        )

        // Then
        assertEquals("OkCredit_AccountStatement_05-11-2020_05-12-2020.pdf", fileName)
    }

    @Test
    fun `execute() given report type CUSTOMER_REPORT should return expected file name`() {
        // Given
        val reportType = DownloadReport.ReportType.CUSTOMER_REPORT
        val startTime = DateTime(1604590843000)
        val endTime = DateTime(1607182945000)

        // When
        val fileName = downloadReportFileNameProvider.execute(
            DownloadReport.Request(
                reportType = reportType,
                startTimeSec = startTime,
                endTimeSec = endTime,
                workName = "worker-1",
            )
        )

        // Then
        assertEquals("OkCredit_CustomerStatement_05-11-2020_05-12-2020.pdf", fileName)
    }

    @Test
    fun `execute() given report type SUPPLIER_REPORT should return expected file name`() {
        // Given
        val reportType = DownloadReport.ReportType.SUPPLIER_REPORT
        val startTime = DateTime(1604590843000)
        val endTime = DateTime(1607182945000)

        // When
        val fileName = downloadReportFileNameProvider.execute(
            DownloadReport.Request(
                reportType = reportType,
                startTimeSec = startTime,
                endTimeSec = endTime,
                workName = "worker-1",
            )
        )

        // Then
        assertEquals("OkCredit_SupplierStatement_05-11-2020_05-12-2020.pdf", fileName)
    }
}
