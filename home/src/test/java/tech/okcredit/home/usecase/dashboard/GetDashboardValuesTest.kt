package tech.okcredit.home.usecase.dashboard

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.home.ui.analytics.HomeEventTracker
import tech.okcredit.home.usecase.dashboard.CollectionValueProvider.Companion.COLLECTION
import tech.okcredit.home.usecase.dashboard.NetBalanceValueProvider.Companion.NET_BALANCE

class GetDashboardValuesTest {

    private val tracker: HomeEventTracker = mock()
    private val netBalanceValueProvider: NetBalanceValueProvider = mock()
    private val collectionValueProvider: CollectionValueProvider = mock()
    private val valueProviders: Map<String, DashboardValueProvider> = mapOf(
        NET_BALANCE to netBalanceValueProvider,
        COLLECTION to collectionValueProvider
    )
    private val getDashboardValues = GetDashboardValues(valueProviders, { tracker })

    private lateinit var recordExceptionSlot: CapturingSlot<Exception>

    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        recordExceptionSlot = slot<Exception>()
        every { RecordException.recordException(capture(recordExceptionSlot)) } returns Unit
    }

    @Test
    fun `execute() when request NET_BALANCE contains should get value from NetBalanceValueProvider`() {
        val req1 = NET_BALANCE to null
        val requestMap = mapOf<String, DashboardValueProvider.Request?>(
            req1
        )
        val res1: DashboardValueProvider.Response = mock()
        whenever(netBalanceValueProvider.getValue(req1.second)).thenReturn(Observable.just(res1))

        val testObserver = getDashboardValues.execute(requestMap).test()

        testObserver.assertValues(hashMapOf(NET_BALANCE to res1))
        verify(netBalanceValueProvider, times(1)).getValue(req1.second)
    }

    @Test
    fun `execute() given multiple requests in map should get value from all respective providers`() {
        val req1 = NET_BALANCE to null
        val req2Duration: DashboardValueProvider.Request = mock()
        val req2 = COLLECTION to req2Duration
        val requestMap = mapOf(
            req1, req2
        )
        val res1: DashboardValueProvider.Response = mock()
        val res2: DashboardValueProvider.Response = mock()
        whenever(netBalanceValueProvider.getValue(req1.second)).thenReturn(Observable.just(res1))
        whenever(collectionValueProvider.getValue(req2Duration)).thenReturn(Observable.just(res2))

        val testObserver = getDashboardValues.execute(requestMap).test()

        testObserver.assertValues(hashMapOf(NET_BALANCE to res1, COLLECTION to res2))
        verify(netBalanceValueProvider, times(1)).getValue(req1.second)
        verify(collectionValueProvider, times(1)).getValue(req2.second)
    }

    @Test
    fun `execute() given empty request map should return empty map`() {
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        val requestMap = mapOf<String, DashboardValueProvider.Request?>()

        val testObserver = getDashboardValues.execute(requestMap).test()

        testObserver.assertValues(hashMapOf())
        verify(netBalanceValueProvider, times(0)).getValue(any())
        verify(collectionValueProvider, times(0)).getValue(any())
        RxJavaPlugins.reset()
    }

    @Test
    fun `execute() given unsupported key in request map should record exception`() {
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        val unsupportedKey = "unsupported_key"
        val req1 = unsupportedKey to null
        val requestMap = mapOf(req1)
        doNothing().whenever(tracker).trackDebug(GetDashboardValues.TAG, "No value provider found for: $unsupportedKey")

        val testObserver = getDashboardValues.execute(requestMap).test()

        testObserver.assertValues(hashMapOf())
        verify(tracker, times(1)).trackDebug(GetDashboardValues.TAG, "No value provider found for: $unsupportedKey")

        io.mockk.verify(exactly = 1) { RecordException.recordException(recordExceptionSlot.captured) }
        assertThat(recordExceptionSlot.captured).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(recordExceptionSlot.captured.message)
            .isEqualTo("${GetDashboardValues.TAG}: No value provider found for: $unsupportedKey")
        RxJavaPlugins.reset()
    }
}
