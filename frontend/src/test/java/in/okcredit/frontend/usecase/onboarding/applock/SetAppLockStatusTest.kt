package `in`.okcredit.frontend.usecase.onboarding.applock

import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.individual.contract.SetIndividualPreference
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_APP_LOCK_SYNCED

class SetAppLockStatusTest {
    private val rxPreference: DefaultPreferences = mock()
    private val setIndividualPreference: SetIndividualPreference = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val onboardingPreferences: OnboardingPreferencesImpl = mock()
    private val setAppLockStatus = SetAppLockStatus(
        { rxPreference },
        { setIndividualPreference },
        { getActiveBusinessId },
        { onboardingPreferences }
    )

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined
    }

    @Test
    fun `when Applock enabled test execute method`() {
        runBlocking {
            val businessId = "business-id"
            // given
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

            // when
            setAppLockStatus.execute(true).test()

            // then
            verify(onboardingPreferences).set(
                eq(OnboardingPreferencesImpl.PREF_INDIVIDUAL_KEY_EXITING_USER_ENABLED_APP_LOCK), eq(false), any()
            )
            verify(onboardingPreferences).set(
                eq(OnboardingPreferences.KEY_APP_LOCK_ENABLED), eq(true), any()
            )
            verify(setIndividualPreference).schedule(
                PreferenceKey.APP_LOCK.key, true.toString(), businessId
            )
            verify(rxPreference).set(eq(OnboardingPreferences.KEY_NEW_USER), eq(false), any())
            verify(rxPreference).set(eq(PREF_INDIVIDUAL_APP_LOCK_SYNCED), eq(true), any())
        }
    }

    @Test
    fun `when AppLock disabled test executemethod`() {
        runBlocking {
            val businessId = "business-id"
            // given
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

            // when
            setAppLockStatus.execute(false).test()

            // then
            verify(onboardingPreferences).set(
                eq(OnboardingPreferencesImpl.PREF_INDIVIDUAL_KEY_EXITING_USER_ENABLED_APP_LOCK), eq(false), any()
            )
            verify(onboardingPreferences).set(
                eq(OnboardingPreferences.KEY_APP_LOCK_ENABLED), eq(false), any()
            )
            verify(setIndividualPreference).schedule(
                PreferenceKey.APP_LOCK.key, false.toString(),
                businessId
            )
            verify(rxPreference).set(eq(PREF_INDIVIDUAL_APP_LOCK_SYNCED), eq(true), any())
        }
    }
}
