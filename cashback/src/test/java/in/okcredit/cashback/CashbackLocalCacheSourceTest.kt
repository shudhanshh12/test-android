package `in`.okcredit.cashback

import `in`.okcredit.cashback.datasource.local.CashbackLocalCacheSourceImpl
import `in`.okcredit.cashback.datasource.local.CashbackLocalCacheSourceImpl.Companion.CASHBACK_DETAILS_MESSAGE_NOT_FOUND
import `in`.okcredit.cashback.datasource.local.CashbackPreferences
import `in`.okcredit.shared.utils.CommonUtils
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ThreadUtils
import java.lang.RuntimeException

class CashbackLocalCacheSourceTest {

    private val cashbackPreferences: CashbackPreferences = mock()
    private val schedulerProvider: SchedulerProvider = mock()
    private val cashbackLocalCacheSource = CashbackLocalCacheSourceImpl { cashbackPreferences }
    private val gson = Gson()

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        mockkObject(CommonUtils)
        whenever(schedulerProvider.io()).thenReturn(Schedulers.trampoline())
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `test when cache does not contain cashback_message_details`() {
        whenever(cashbackPreferences.getCashbackMessageDetails()).thenReturn(Observable.just(""))

        cashbackLocalCacheSource.getCachedCashbackMessageDetails().test().apply {
            assertError(RuntimeException::class.java)
            assertErrorMessage(CASHBACK_DETAILS_MESSAGE_NOT_FOUND)
            dispose()
        }
    }

    @Test
    fun `test when cache contains cashback_message_details`() {
        val cashbackMessageDetailsDto = TestData.cashbackMessageDetailsDto
        val serialisedObj = gson.toJson(cashbackMessageDetailsDto)

        whenever(cashbackPreferences.getCashbackMessageDetails()).thenReturn(Observable.just(serialisedObj))

        cashbackLocalCacheSource.getCachedCashbackMessageDetails().test().apply {
            assertNoErrors()
            assertValue(cashbackMessageDetailsDto)
            dispose()
        }
    }

    @Test
    fun `test to set cashback_message_details in cache`() {
        val cashbackMessageDetailsDto = TestData.cashbackMessageDetailsDto
        val millis: Long = 12345
        val json = gson.toJson(cashbackMessageDetailsDto)

        every { CommonUtils.currentDateTime().millis } returns millis
        whenever(cashbackPreferences.setCashbackMessageDetails(json)).thenReturn(Completable.complete())
        whenever(cashbackPreferences.setCashbackMessageDetailsTimestamp(millis)).thenReturn(Completable.complete())

        cashbackLocalCacheSource.setCashbackMessageDetailsCache(cashbackMessageDetailsDto).test().apply {
            verify(cashbackPreferences, times(1)).setCashbackMessageDetails(json)
            verify(cashbackPreferences, times(1)).setCashbackMessageDetailsTimestamp(millis)
            assertComplete()
            dispose()
        }
    }

    @Test
    fun `test when cache does not contain cashback_message_details timestamp`() {
        whenever(cashbackPreferences.getCashbackMessageDetailsTimestamp()).thenReturn(Observable.just(-1))

        cashbackLocalCacheSource.getCachedCashbackMessageDetailsTimestamp().test().apply {
            assertNoErrors()
            assertValue(-1)
            dispose()
        }
    }

    @Test
    fun `test when cache contains cashback_message_details timestamp`() {
        whenever(cashbackPreferences.getCashbackMessageDetailsTimestamp()).thenReturn(Observable.just(111))

        cashbackLocalCacheSource.getCachedCashbackMessageDetailsTimestamp().test().apply {
            assertNoErrors()
            assertValue(111)
            dispose()
        }
    }
}
