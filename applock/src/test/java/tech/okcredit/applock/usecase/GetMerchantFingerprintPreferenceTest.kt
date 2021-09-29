package tech.okcredit.applock.usecase

import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.backend.contract.RxSharedPrefValues.FINGERPRINT_LOCK_ENABLED
import `in`.okcredit.backend.contract.RxSharedPrefValues.FINGERPRINT_LOCK_SYNCED
import `in`.okcredit.individual.contract.PreferenceKey
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import tech.okcredit.android.base.preferences.DefaultPreferences

class GetMerchantFingerprintPreferenceTest {
    private val getMerchantPreference: GetMerchantPreference = mock()
    private val rxSharedPreference: DefaultPreferences = mock()
    private val getMerchantFingerprintPreference = GetMerchantFingerprintPreference(
        { getMerchantPreference },
        { rxSharedPreference }
    )

    @Test
    fun executeTest() {
        runBlocking {
            whenever(rxSharedPreference.getBoolean(eq(FINGERPRINT_LOCK_SYNCED), any(), anyOrNull())).thenReturn(flowOf(true))
            whenever(rxSharedPreference.getBoolean(eq(FINGERPRINT_LOCK_ENABLED), any(), anyOrNull())).thenReturn(flowOf(true))

            val result = getMerchantFingerprintPreference.execute().test()

            result.assertValue(true)
        }
    }

    @Test
    fun executeTestFalse() {
        runBlocking {
            whenever(rxSharedPreference.getBoolean(eq(FINGERPRINT_LOCK_SYNCED), any(), anyOrNull())).thenReturn(flowOf(true))
            whenever(rxSharedPreference.getBoolean(eq(FINGERPRINT_LOCK_ENABLED), any(), anyOrNull())).thenReturn(flowOf(false))

            val result = getMerchantFingerprintPreference.execute().test()

            result.assertValue(false)
        }
    }

    @Test
    fun executeFingerprintLockFalse() {
        runBlocking {
            whenever(rxSharedPreference.getBoolean(eq(FINGERPRINT_LOCK_SYNCED), any(), anyOrNull()))
                .thenReturn(flowOf(false))
            whenever(getMerchantPreference.execute(PreferenceKey.FINGER_PRINT_LOCK))
                .thenReturn(Observable.just("true"))
            whenever(rxSharedPreference.getBoolean(eq(FINGERPRINT_LOCK_ENABLED), any(), anyOrNull()))
                .thenReturn(flowOf(true))

            val result = getMerchantFingerprintPreference.execute().test().awaitCount(1)

            result.assertValue(true)
        }
    }

    @Test
    fun executeFingerprintSyncFalse() {
        runBlocking {
            whenever(rxSharedPreference.getBoolean(eq(FINGERPRINT_LOCK_SYNCED), any(), anyOrNull()))
                .thenReturn(flowOf(false))
            whenever(getMerchantPreference.execute(PreferenceKey.FINGER_PRINT_LOCK))
                .thenReturn(Observable.just("false"))
            whenever(rxSharedPreference.getBoolean(eq(FINGERPRINT_LOCK_ENABLED), any(), anyOrNull()))
                .thenReturn(flowOf(false))

            val result = getMerchantFingerprintPreference.execute().test().awaitCount(1)

            result.assertValue(false)
        }
    }
}
