package tech.okcredit.home.usecase

import `in`.okcredit.backend._offline.usecase.SetMerchantPreference
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.preferences.DefaultPreferences

class SetMerchantAppLockPreferenceTest {
    private val rxPreference: DefaultPreferences = mock()
    private val setMerchantPreference: SetMerchantPreference = mock()
    private val onboardingPreferences: OnboardingPreferences = mock()

    private val setMerchantAppLockPreference =
        SetMerchantAppLockPreference(rxPreference, setMerchantPreference, Lazy { onboardingPreferences })

    companion object {
        const val KEY_APP_LOCK_SYNCED = "KEY_APP_LOCK_SYNCED"
        val APP_LOCK = PreferenceKey.APP_LOCK
    }

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined
    }

    @Test
    fun `when key app lock sync done`() {
        // given
        whenever(rxPreference.getBoolean(eq(KEY_APP_LOCK_SYNCED), any(), anyOrNull())).thenReturn(flowOf(true))
        whenever(onboardingPreferences.isAppLockEnabled()).thenReturn(true)
        whenever(setMerchantPreference.execute(APP_LOCK, "true")).thenReturn(Completable.complete())

        // when
        val testObserver = setMerchantAppLockPreference.execute(Unit).subscribeOn(Schedulers.trampoline()).test()

        // then
        testObserver.assertComplete()
        testObserver.dispose()
    }

    @Test
    fun `when key app lock sync not done`() {
        // given
        whenever(rxPreference.getBoolean(eq(KEY_APP_LOCK_SYNCED), any(), anyOrNull())).thenReturn(flowOf(false))
        whenever(onboardingPreferences.isAppLockEnabled()).thenReturn(true)
        whenever(setMerchantPreference.execute(APP_LOCK, "true")).thenReturn(Completable.complete())

        // when
        val testObserver = setMerchantAppLockPreference.execute(Unit).subscribeOn(Schedulers.trampoline()).test()

        // then
        testObserver.assertComplete()
        testObserver.dispose()
    }
}
