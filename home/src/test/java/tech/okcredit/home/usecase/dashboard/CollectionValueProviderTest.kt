package tech.okcredit.home.usecase.dashboard

import `in`.okcredit.collection.contract.CreditGraphicalDataProvider
import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.okcredit.home.R

class CollectionValueProviderTest {

    private val creditGraphicalDataProvider: CreditGraphicalDataProvider = mock()
    private val context: Context = mock()
    private val creditGraphicalDataProviderLazy: Lazy<CreditGraphicalDataProvider> =
        Lazy { creditGraphicalDataProvider }
    private val contextLazy: Lazy<Context> = Lazy { context }
    private val collectionValueProvider = CollectionValueProvider(creditGraphicalDataProviderLazy, contextLazy)

    private val stringToday = "Today"
    private val stringYesterday = "Yesterday"
    private val string7days = "Last 7 days"
    private val string30days = "Last 30 days"

    @Before
    fun setUp() {
        whenever(context.getString(R.string.today)).thenReturn(stringToday)
        whenever(context.getString(R.string.yesterday)).thenReturn(stringYesterday)
        whenever(context.getString(R.string.last_seven_days)).thenReturn(string7days)
        whenever(context.getString(R.string.last_thirty_days)).thenReturn(string30days)
    }

    @Test
    fun `getValue() given feature is enabled when request duration is 0 should return value for TODAY`() {
        val req: DashboardValueProvider.Request = mock()
        whenever(req.input).thenReturn(0)
        val duration = CreditGraphicalDataProvider.GraphDuration.TODAY
        val graphResponse: CreditGraphicalDataProvider.GraphResponse = mock()
        whenever(creditGraphicalDataProvider.execute(duration)).thenReturn(Observable.just(graphResponse))
        val offlineCollection = 1000L
        val onlineCollection = 5000L
        whenever(graphResponse.offlineCollection).thenReturn(offlineCollection)
        whenever(graphResponse.onlineCollection).thenReturn(onlineCollection)
        whenever(graphResponse.graphDuration).thenReturn(duration)

        val testObserver = collectionValueProvider.getValue(req).test()

        val result = testObserver.values().first() as CollectionValueProvider.CollectionDashboardValue
        assertTrue(result.value == offlineCollection + onlineCollection)
        assertTrue(result.string == stringToday)
    }

    @Test
    fun `getValue() given feature is enabled when request duration is 1 should return value for YESTERDAY`() {
        val req: DashboardValueProvider.Request = mock()
        whenever(req.input).thenReturn(1)
        val duration = CreditGraphicalDataProvider.GraphDuration.YESTERDAY
        val graphResponse: CreditGraphicalDataProvider.GraphResponse = mock()
        whenever(creditGraphicalDataProvider.execute(duration)).thenReturn(Observable.just(graphResponse))
        val offlineCollection = 1000L
        val onlineCollection = 5000L
        whenever(graphResponse.offlineCollection).thenReturn(offlineCollection)
        whenever(graphResponse.onlineCollection).thenReturn(onlineCollection)
        whenever(graphResponse.graphDuration).thenReturn(duration)

        val testObserver = collectionValueProvider.getValue(req).test()

        val result = testObserver.values().first() as CollectionValueProvider.CollectionDashboardValue
        assertTrue(result.value == offlineCollection + onlineCollection)
        assertTrue(result.string == stringYesterday)
    }

    @Test
    fun `getValue() given feature is enabled when request duration is 7 should return value for WEEK`() {
        val req: DashboardValueProvider.Request = mock()
        whenever(req.input).thenReturn(7)
        val duration = CreditGraphicalDataProvider.GraphDuration.WEEK
        val graphResponse: CreditGraphicalDataProvider.GraphResponse = mock()
        whenever(creditGraphicalDataProvider.execute(duration)).thenReturn(Observable.just(graphResponse))
        val offlineCollection = 1000L
        val onlineCollection = 5000L
        whenever(graphResponse.offlineCollection).thenReturn(offlineCollection)
        whenever(graphResponse.onlineCollection).thenReturn(onlineCollection)
        whenever(graphResponse.graphDuration).thenReturn(duration)

        val testObserver = collectionValueProvider.getValue(req).test()

        val result = testObserver.values().first() as CollectionValueProvider.CollectionDashboardValue
        assertTrue(result.value == offlineCollection + onlineCollection)
        assertTrue(result.string == string7days)
    }

    @Test
    fun `getValue() given feature is enabled when request duration is 30 should return value for MONTH`() {
        val req: DashboardValueProvider.Request = mock()
        whenever(req.input).thenReturn(30)
        val duration = CreditGraphicalDataProvider.GraphDuration.MONTH
        val graphResponse: CreditGraphicalDataProvider.GraphResponse = mock()
        whenever(creditGraphicalDataProvider.execute(duration)).thenReturn(Observable.just(graphResponse))
        val offlineCollection = 1000L
        val onlineCollection = 5000L
        whenever(graphResponse.offlineCollection).thenReturn(offlineCollection)
        whenever(graphResponse.onlineCollection).thenReturn(onlineCollection)
        whenever(graphResponse.graphDuration).thenReturn(duration)

        val testObserver = collectionValueProvider.getValue(req).test()

        val result = testObserver.values().first() as CollectionValueProvider.CollectionDashboardValue
        assertTrue(result.value == offlineCollection + onlineCollection)
        assertTrue(result.string == string30days)
    }

    @Test
    fun `getValue() given feature is enabled when request duration is null should return value for WEEK`() {
        val duration = CreditGraphicalDataProvider.GraphDuration.WEEK
        val graphResponse: CreditGraphicalDataProvider.GraphResponse = mock()
        whenever(creditGraphicalDataProvider.execute(duration)).thenReturn(Observable.just(graphResponse))
        val offlineCollection = 1000L
        val onlineCollection = 5000L
        whenever(graphResponse.offlineCollection).thenReturn(offlineCollection)
        whenever(graphResponse.onlineCollection).thenReturn(onlineCollection)
        whenever(graphResponse.graphDuration).thenReturn(duration)

        val testObserver = collectionValueProvider.getValue(null).test()

        val result = testObserver.values().first() as CollectionValueProvider.CollectionDashboardValue
        assertTrue(result.value == offlineCollection + onlineCollection)
        assertTrue(result.string == string7days)
    }
}
