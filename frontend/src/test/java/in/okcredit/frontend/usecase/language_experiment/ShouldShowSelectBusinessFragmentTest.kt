package `in`.okcredit.frontend.usecase.language_experiment

import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.contract.IsMultipleAccountEnabled
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ShouldShowSelectBusinessFragmentTest {

    private val getBusinessIdList: GetBusinessIdList = mock()
    private val isMultipleAccountEnabled: IsMultipleAccountEnabled = mock()
    private val shouldShowSelectBusinessFragment =
        ShouldShowSelectBusinessFragment({ getBusinessIdList }, { isMultipleAccountEnabled })

    @Test
    fun `given 2 business present and MultipleAccount feature enabled should return true`() {
        runBlocking {
            whenever(getBusinessIdList.execute()).thenReturn(flowOf(listOf("businessid1", "businessid2")))
            whenever(isMultipleAccountEnabled.execute()).thenReturn(Observable.just(true))

            val show = shouldShowSelectBusinessFragment.execute()

            assertTrue(show)
            verify(getBusinessIdList).execute()
            verify(isMultipleAccountEnabled).execute()
        }
    }

    @Test
    fun `given 1 business present and MultipleAccount feature enabled should return false`() {
        runBlocking {
            whenever(getBusinessIdList.execute()).thenReturn(flowOf(listOf("businessid1")))
            whenever(isMultipleAccountEnabled.execute()).thenReturn(Observable.just(true))

            val show = shouldShowSelectBusinessFragment.execute()

            assertFalse(show)
            verify(getBusinessIdList).execute()
            verify(isMultipleAccountEnabled).execute()
        }
    }

    @Test
    fun `given 0 business present and MultipleAccount feature enabled should return false`() {
        runBlocking {
            whenever(getBusinessIdList.execute()).thenReturn(flowOf(listOf()))
            whenever(isMultipleAccountEnabled.execute()).thenReturn(Observable.just(true))

            val show = shouldShowSelectBusinessFragment.execute()

            assertFalse(show)
            verify(getBusinessIdList).execute()
            verify(isMultipleAccountEnabled).execute()
        }
    }

    @Test
    fun `given 2 business present and MultipleAccount feature not enabled should return false`() {
        runBlocking {
            whenever(getBusinessIdList.execute()).thenReturn(flowOf(listOf("businessid1", "businessid2")))
            whenever(isMultipleAccountEnabled.execute()).thenReturn(Observable.just(false))

            val show = shouldShowSelectBusinessFragment.execute()

            assertFalse(show)
            verify(getBusinessIdList).execute()
            verify(isMultipleAccountEnabled).execute()
        }
    }
}
