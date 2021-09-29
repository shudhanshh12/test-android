package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.store.sharedprefs.BusinessPreferences
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SetActiveBusinessIdTest {

    private val rxSharedPreference: BusinessPreferences = mock()
    private val setDefaultBusinessIdImpl = SetActiveBusinessIdImpl { rxSharedPreference }

    @Test
    fun `when default business id not set & logged in execute then get merchant id and set in shared prefs`() {
        runBlocking {
            // Given
            mockkStatic(Dispatchers::class)
            every { Dispatchers.Default } returns Dispatchers.Unconfined

            whenever(rxSharedPreference.contains(eq("default_business_id"), any())).thenReturn(false)
            val id = "sample-id"

            // When
            val testObserver = setDefaultBusinessIdImpl.execute(id).test()

            // Then
            testObserver.assertComplete()
            verify(rxSharedPreference).set(eq("default_business_id"), eq(id), any())
        }
    }
}
