package tech.okcredit.home.usecase

import `in`.okcredit.individual.contract.SetIndividualPreference
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Test
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.home.ui.settings.usecase.SetFingerprintLockStatus

class SetFingerPrintLockStatusTest {
    private val rxSharedPreference: DefaultPreferences = mock()
    private val setIndividualPreference: SetIndividualPreference = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val setFingerPrintLockStatus = SetFingerprintLockStatus(
        { rxSharedPreference },
        { setIndividualPreference },
        { getActiveBusinessId }
    )

    @Test
    fun testExecuteTrue() {
        runBlocking {
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

            val result = setFingerPrintLockStatus.execute(true).test().awaitCount(1)

            verify(rxSharedPreference).set(eq("FINGERPRINT_LOCK_ENABLED"), eq(true), any())
            verify(rxSharedPreference).set(eq("FINGERPRINT_LOCK_SYNCED"), eq(true), any())
            result.assertComplete()
        }
    }

    @Test
    fun testExecuteFalse() {
        runBlocking {
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

            val result = setFingerPrintLockStatus.execute(false).test().awaitCount(1)

            verify(rxSharedPreference).set(eq("FINGERPRINT_LOCK_ENABLED"), eq(false), any())
            verify(rxSharedPreference).set(eq("FINGERPRINT_LOCK_SYNCED"), eq(true), any())
            result.assertComplete()
        }
    }
}
