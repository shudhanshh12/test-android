package `in`.okcredit.backend._offline.usecase.reports_v2

import `in`.okcredit.backend._offline.server.internal.GenerateReportUrlResponse
import `in`.okcredit.backend._offline.server.internal.GetReportUrlResponse
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport.Companion.DOWNLOAD_REPORT_INTERVAL_IN_SECONDS_KEY
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.device.Device
import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.shared.service.rxdownloader.RxDownloader
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.OkcWorkManager

class DownloadReportTest {

    private val workManager: OkcWorkManager = mock()
    private val rxDownloader: RxDownloader = mock()
    private val localeManager: LocaleManager = mock()
    private val deviceRepository: DeviceRepository = mock()
    private val reportsV2Repository: ReportsV2Repository = mock()
    private val downloadReportFileNameProvider: DownloadReportFileNameProvider = mock()
    private val tracker: ReportsV2Tracker = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessId = "businessId"

    private val downloadReport = DownloadReport(
        { workManager },
        { rxDownloader },
        { localeManager },
        { deviceRepository },
        { reportsV2Repository },
        { downloadReportFileNameProvider },
        { tracker },
        { firebaseRemoteConfig },
        { getActiveBusinessId }
    )

    @Before
    fun setup() {
        val device = mock<Device>().apply {
            whenever(this.id).thenReturn("device-id")
        }
        whenever(deviceRepository.getDevice()).thenReturn(Observable.just(device))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        mockkObject(ThreadUtils)
        every { ThreadUtils.database() } returns Schedulers.trampoline()
        every { ThreadUtils.newThread() } returns Schedulers.trampoline()
        every { ThreadUtils.io() } returns Schedulers.trampoline()
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun `execute() given report type is BACKUP_ALL, api succeeds in 1 try should download file and return downloaded file uri string`() {
        // Given
        val reportType = DownloadReport.ReportType.BACKUP_ALL
        val reportId = "report-id"
        val generateReportUrlResponse = mock<GenerateReportUrlResponse>().apply {
            whenever(this.reportId).thenReturn(reportId)
        }
        whenever(firebaseRemoteConfig.getString(DOWNLOAD_REPORT_INTERVAL_IN_SECONDS_KEY)).thenReturn("5")
        whenever(reportsV2Repository.generateReportUrl(any(), eq(businessId)))
            .thenReturn(Single.just(generateReportUrlResponse))
        val reportUrl = "report-url"
        val getReportUrlResponse = mock<GetReportUrlResponse>().apply {
            whenever(this.status).thenReturn("success")
            whenever(this.reportUrl).thenReturn(reportUrl)
            whenever(this.reportId).thenReturn(reportId)
        }
        whenever(localeManager.getLanguage()).thenReturn("en")
        whenever(reportsV2Repository.getReportUrl(reportId, businessId)).thenReturn(Single.just(getReportUrlResponse))
        val downloadedFileUriString = "downloaded-file-uri-string"
        whenever(rxDownloader.download(any(), any(), any(), any())).thenReturn(Single.just(downloadedFileUriString))
        val fileName = "file-name"

        // When
        val testObserver = downloadReport.execute(
            accountId = null,
            startTimeSec = -1,
            endTimeSec = -1,
            fileName = fileName,
            reportTypeServerKey = reportType.typeKeywordAtServer,
            reportTypeName = reportType.name,
            businessId = businessId
        ).test()

        // Then
        testObserver.assertValue(downloadedFileUriString)
        verify(deviceRepository).getDevice()
        verify(tracker).trackWorkerStarted(eq(reportType.name), anyOrNull(), any(), any())
        verify(reportsV2Repository).generateReportUrl(any(), eq(businessId))
        verify(reportsV2Repository).getReportUrl(reportId, businessId)
        verify(rxDownloader).download(eq(reportUrl), eq(fileName), any(), any())
        verify(tracker).trackSuccess(eq(reportType.name), eq(1), anyOrNull(), any(), any())
    }

    @Test
    fun `execute() given report type is CUSTOMER_ACCOUNT, api succeeds in multiple tries should download file and return downloaded file uri string`() {
        // Given
        val reportType = DownloadReport.ReportType.CUSTOMER_ACCOUNT
        val reportId = "report-id"
        val generateReportUrlResponse = mock<GenerateReportUrlResponse>().apply {
            whenever(this.reportId).thenReturn(reportId)
        }
        whenever(firebaseRemoteConfig.getString(DOWNLOAD_REPORT_INTERVAL_IN_SECONDS_KEY)).thenReturn("5")
        whenever(reportsV2Repository.generateReportUrl(any(), eq(businessId))).thenReturn(Single.just(generateReportUrlResponse))
        val reportUrl = "report-url"
        val getReportUrlResponse = mock<GetReportUrlResponse>().apply {
            whenever(this.status).thenReturn("open").thenReturn("open").thenReturn("success")
            whenever(this.reportUrl).thenReturn(reportUrl)
            whenever(this.reportId).thenReturn(reportId)
        }
        whenever(localeManager.getLanguage()).thenReturn("en")
        whenever(reportsV2Repository.getReportUrl(reportId, businessId)).thenReturn(Single.just(getReportUrlResponse))
        val downloadedFileUriString = "downloaded-file-uri-string"
        whenever(rxDownloader.download(any(), any(), any(), any())).thenReturn(Single.just(downloadedFileUriString))
        val fileName = "file-name"

        // When
        val testObserver = downloadReport.execute(
            accountId = null,
            startTimeSec = -1,
            endTimeSec = -1,
            fileName = fileName,
            reportTypeServerKey = reportType.typeKeywordAtServer,
            reportTypeName = reportType.name,
            businessId = businessId
        ).test()

        // Then
        testObserver.assertValue(downloadedFileUriString)
        verify(deviceRepository).getDevice()
        verify(tracker).trackWorkerStarted(eq(reportType.name), anyOrNull(), any(), any())
        verify(reportsV2Repository).generateReportUrl(any(), eq(businessId))
        verify(reportsV2Repository, times(2)).getReportUrl(reportId, businessId)
        verify(rxDownloader).download(eq(reportUrl), eq(fileName), any(), any())
        verify(tracker).trackSuccess(eq(reportType.name), eq(2), anyOrNull(), any(), any())
    }
}
