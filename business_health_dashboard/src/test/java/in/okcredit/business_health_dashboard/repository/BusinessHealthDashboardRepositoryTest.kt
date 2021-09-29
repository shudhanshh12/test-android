package `in`.okcredit.business_health_dashboard.repository

import `in`.okcredit.business_health_dashboard.datasource.local.BusinessHealthDashboardLocalSource
import `in`.okcredit.business_health_dashboard.datasource.local.BusinessHealthDashboardNoLocalDataException
import `in`.okcredit.business_health_dashboard.datasource.remote.BusinessHealthDashboardRemoteSource
import `in`.okcredit.business_health_dashboard.testdata.TestData
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException

class BusinessHealthDashboardRepositoryTest {

    private val businessHealthDashboardRemoteSourceMock: BusinessHealthDashboardRemoteSource = mockk()
    private val businessHealthDashboardLocalSourceMock: BusinessHealthDashboardLocalSource = mockk()

    lateinit var businessHealthDashboardRepository: BusinessHealthDashboardRepository

    private val businessId = "dummy_business_id"

    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        businessHealthDashboardRepository = spyk(
            BusinessHealthDashboardRepository(
                { businessHealthDashboardRemoteSourceMock },
                { businessHealthDashboardLocalSourceMock },
            )
        )
    }

    /**
     * Steps:
     * 1. Data not present in local
     * 2. Hence, fetch from remote
     * 3. Re-subscribe to get data from local
     */
    @Test
    fun `getBusinessHealthDashboardData - when data not present in local, emit exception, fetch from remote, resubscribe local`() {
        every { businessHealthDashboardLocalSourceMock.getBusinessHealthDashboardData(businessId) } returns Observable.error(
            BusinessHealthDashboardNoLocalDataException
        ) andThen Observable.empty()
        every { businessHealthDashboardLocalSourceMock.getUserPreferredTimeCadence(businessId) } returns Observable.empty()
        every { businessHealthDashboardRepository.fetchFromRemoteAndSaveToLocal(businessId) } returns Completable.complete()

        val result = businessHealthDashboardRepository.getBusinessHealthDashboardData(businessId).test()

        assertEquals(0, result.valueCount())
        verify(exactly = 1) { businessHealthDashboardRepository.fetchFromRemoteAndSaveToLocal(businessId) }
        verify(exactly = 2) { businessHealthDashboardLocalSourceMock.getBusinessHealthDashboardData(businessId) }
        result.dispose()
    }

    /**
     * Steps:
     * 1. Fetch data present in local
     * 2. No user preferred time cadence present (return default value)
     * 3. Emit domain object (same object that was emitted from local)
     */
    @Test
    fun `getBusinessHealthDashboardData - when data present in local, no user preferred time cadence present`() {
        every { businessHealthDashboardLocalSourceMock.getBusinessHealthDashboardData(businessId) } returns Observable.just(
            TestData.BUSINESS_HEALTH_DASHBOARD_MODEL
        )
        every { businessHealthDashboardLocalSourceMock.getUserPreferredTimeCadence(businessId) } returns Observable.just(
            ""
        )

        val result = businessHealthDashboardRepository.getBusinessHealthDashboardData(businessId).test()

        assertEquals(1, result.valueCount())
        assertEquals(TestData.BUSINESS_HEALTH_DASHBOARD_MODEL, result.values()[0])

        verify(exactly = 0) { businessHealthDashboardRepository.fetchFromRemoteAndSaveToLocal(businessId) }
        verify(exactly = 1) { businessHealthDashboardLocalSourceMock.getBusinessHealthDashboardData(businessId) }
        result.dispose()
    }

    /**
     * Steps:
     * 1. Fetch data present in local
     * 2. No user preferred time cadence present (return default value)
     * 3. Emit domain object but with overwritten selected time cadence object
     */
    @Test
    fun `getBusinessHealthDashboardData - when data present in local, user preferred time cadence present, overwrite selected time cadence`() {
        val dataFromLocal = TestData.BUSINESS_HEALTH_DASHBOARD_MODEL.copy(
            selectedTimeCadence = TestData.cadence1
        )

        val userPreferredTimeCadenceString = TestData.cadence2.title

        every { businessHealthDashboardLocalSourceMock.getBusinessHealthDashboardData(businessId) } returns Observable.just(
            dataFromLocal
        )
        every { businessHealthDashboardLocalSourceMock.getUserPreferredTimeCadence(businessId) } returns Observable.just(
            userPreferredTimeCadenceString
        )

        val result = businessHealthDashboardRepository.getBusinessHealthDashboardData(businessId).test()

        assertEquals(1, result.valueCount())
        assertEquals(
            TestData.BUSINESS_HEALTH_DASHBOARD_MODEL.copy(
                selectedTimeCadence = TestData.cadence2
            ),
            result.values()[0]
        )

        verify(exactly = 0) { businessHealthDashboardRepository.fetchFromRemoteAndSaveToLocal(businessId) }
        verify(exactly = 1) { businessHealthDashboardLocalSourceMock.getBusinessHealthDashboardData(businessId) }
        result.dispose()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }
}
