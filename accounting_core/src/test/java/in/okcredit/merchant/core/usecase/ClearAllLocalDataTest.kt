package `in`.okcredit.merchant.core.usecase

import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.core.store.CoreLocalSource
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import tech.okcredit.android.base.preferences.DefaultPreferences

class ClearAllLocalDataTest {
    private val coreLocalSource: CoreLocalSource = mock()
    private val getBusinessIdList: GetBusinessIdList = mock()
    private val defaultPreferences: DefaultPreferences = mock()

    private val clearAllLocalData = ClearAllLocalData(
        { coreLocalSource },
        { getBusinessIdList },
        { defaultPreferences }
    )

    companion object {
        private const val PREF_KEY = "core_sdk_enabled"
    }

    @Test
    fun `execute should complete`() {
        runBlocking {
            val businessIdList = listOf("business-1", "business-2")
            mockkStatic(Dispatchers::class)
            every { Dispatchers.Default } returns Dispatchers.Unconfined
            whenever(coreLocalSource.clearCommandTable()).thenReturn(Completable.complete())
            whenever(coreLocalSource.clearTransactionTable()).thenReturn(Completable.complete())
            whenever(coreLocalSource.clearCustomerTable()).thenReturn(Completable.complete())
            whenever(getBusinessIdList.execute()).thenReturn(flowOf(businessIdList))
            whenever(defaultPreferences.remove(eq(PREF_KEY), any())).thenReturn(true)
            val testObserver = clearAllLocalData.execute().test()

            verify(coreLocalSource).clearCommandTable()
            verify(coreLocalSource).clearTransactionTable()
            verify(coreLocalSource).clearCustomerTable()
            verify(getBusinessIdList).execute()
            verify(defaultPreferences, times(2)).remove(eq(PREF_KEY), any())

            testObserver.assertComplete()
        }
    }
}
