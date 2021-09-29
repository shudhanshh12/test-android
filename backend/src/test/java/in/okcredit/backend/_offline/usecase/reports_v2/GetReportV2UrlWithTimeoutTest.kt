package `in`.okcredit.backend._offline.usecase.reports_v2

import `in`.okcredit.backend._offline.server.internal.GetReportUrlResponse
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.utils.TimeUtils.toSeconds
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class GetReportV2UrlWithTimeoutTest {
    private val downloadReport: DownloadReport = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val getReportV2UrlWithTimeout: GetReportV2UrlWithTimeout =
        GetReportV2UrlWithTimeout({ downloadReport }, { getActiveBusinessId }, { firebaseRemoteConfig })
    lateinit var testScheduler: TestScheduler

    private val startTime = 1L
    private val endTime = 2L
    private val accountId = "accountId"
    private val businessId = "businessId"
    private val reportTypeServerKey = "reportTypeServerKey"
    private val dummyReportUrl = "reportUrl"
    private val timeOutInSecond = 1L

    @Before
    fun setup() {
        testScheduler = TestScheduler()
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns testScheduler
        every { Schedulers.computation() } returns testScheduler
    }

    @Test
    fun `when before timeout response is returned should execute successfully`() {

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        whenever(firebaseRemoteConfig.getLong("share_report_max_polling_time_in_seconds")).thenReturn(timeOutInSecond)

        val getReportUrlResponse = mock<GetReportUrlResponse>().apply {
            whenever(this.status).thenReturn("success")
            whenever(this.reportUrl).thenReturn(dummyReportUrl)
            whenever(this.reportId).thenReturn("reportId")
        }
        whenever(
            downloadReport.getReportV2Url(
                accountId,
                startTime.toSeconds(),
                endTime.toSeconds(),
                reportTypeServerKey,
                businessId,
            )
        ).thenReturn(Single.just(getReportUrlResponse).delay(timeOutInSecond.minus(1), TimeUnit.SECONDS))

        val testObserver = getReportV2UrlWithTimeout.execute(
            accountId,
            startTime,
            endTime,
            reportTypeServerKey,
        ).test()

        testScheduler.advanceTimeBy(timeOutInSecond, TimeUnit.SECONDS)

        testObserver.assertValue(dummyReportUrl)

        verify(downloadReport).getReportV2Url(
            accountId,
            startTime.toSeconds(),
            endTime.toSeconds(),
            reportTypeServerKey,
            businessId,
        )
        verify(getActiveBusinessId).execute()

        verify(firebaseRemoteConfig).getLong("share_report_max_polling_time_in_seconds")

        testObserver.dispose()
    }

    @Test
    fun `when timeout TimeOutException Should Be Thrown `() {

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        whenever(firebaseRemoteConfig.getLong("share_report_max_polling_time_in_seconds")).thenReturn(timeOutInSecond)

        val getReportUrlResponse = mock<GetReportUrlResponse>().apply {
            whenever(this.status).thenReturn("success")
            whenever(this.reportUrl).thenReturn(dummyReportUrl)
            whenever(this.reportId).thenReturn("reportId")
        }
        whenever(
            downloadReport.getReportV2Url(
                accountId,
                startTime.toSeconds(),
                endTime.toSeconds(),
                reportTypeServerKey,
                businessId,
            )
        ).thenReturn(Single.just(getReportUrlResponse).delay(timeOutInSecond, TimeUnit.SECONDS))

        val testObserver = getReportV2UrlWithTimeout.execute(
            accountId,
            startTime,
            endTime,
            reportTypeServerKey,
        ).test()

        testScheduler.advanceTimeBy(timeOutInSecond, TimeUnit.SECONDS)

        testObserver.assertError(TimeoutException::class.java)

        verify(downloadReport).getReportV2Url(
            accountId,
            startTime.toSeconds(),
            endTime.toSeconds(),
            reportTypeServerKey,
            businessId,
        )
        verify(getActiveBusinessId).execute()

        verify(firebaseRemoteConfig).getLong("share_report_max_polling_time_in_seconds")

        testObserver.dispose()
    }
}
