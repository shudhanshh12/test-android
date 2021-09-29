package tech.okcredit.home.settings

import `in`.okcredit.backend.contract.AppLockManager
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import org.junit.Test
import tech.okcredit.home.ui.settings.usecase.CheckAppLock

class CheckAppLockTest {
    private val onboardingPreferences: OnboardingPreferences = mock()
    private val appLockManager: AppLockManager = mock()

    private val checkAppLock = CheckAppLock(Lazy { onboardingPreferences }, Lazy { appLockManager })

    companion object {
        const val CUSTOM_APP_LOCK = "CUSTOM_APP_LOCK"
        const val SYSTEM_APP_LOCK = "SYSTEM_APP_LOCK"
        const val NO_APP_LOCK = "NO_APP_LOCK"
    }
    @Test
    fun `execute Test`() {
        // given
        whenever(appLockManager.isAppLockActive()).thenReturn(true)

        // when
        val result = checkAppLock.execute().test()

        // then
        result.assertValue(Pair(true, CUSTOM_APP_LOCK))
    }

    @Test
    fun `execute  return SYSTEM_APP_LOCK`() {
        // given
        whenever(appLockManager.isAppLockActive()).thenReturn(false)
        whenever(onboardingPreferences.isAppLockEnabled()).thenReturn(true)

        // when
        val result = checkAppLock.execute().test()

        // then
        result.assertValue(Pair(true, SYSTEM_APP_LOCK))
    }

    @Test
    fun `execute  return NO_APP_LOCK`() {
        // given
        whenever(appLockManager.isAppLockActive()).thenReturn(false)
        whenever(onboardingPreferences.isAppLockEnabled()).thenReturn(false)

        // when
        val result = checkAppLock.execute().test()

        // then
        result.assertValue(Pair(false, NO_APP_LOCK))
    }
}
