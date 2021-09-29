package `in`.okcredit.frontend.usecase.onboarding.applock

import `in`.okcredit.backend.contract.AppLockManager
import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.frontend.contract.data.AppResume
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import org.junit.Test
import tech.okcredit.android.base.AppVariable

class CheckAppLockAuthenticationImplTest {
    private val appVariable: AppVariable = mock()
    private val appVariableLazy: Lazy<AppVariable> = Lazy { appVariable }
    private var appLockManager: AppLockManager = mock()
    private val appLockManagerLazy: Lazy<AppLockManager> = Lazy { appLockManager }
    private val onboardingPreferences: OnboardingPreferencesImpl = mock()
    private val onboardingPreferencesLazy: Lazy<OnboardingPreferencesImpl> = Lazy { onboardingPreferences }
    private val checkAuth: CheckAuth = mock()
    private val checkAuthLazy: Lazy<CheckAuth> = Lazy { checkAuth }
    private val checkAppLockAuthentication = CheckAppLockAuthenticationImpl(
        appVariableLazy,
        appLockManagerLazy,
        onboardingPreferencesLazy,
        checkAuthLazy
    )

    @Test
    fun `should return resume type Not Authenticated`() {
        whenever(checkAuthLazy.get().execute())
            .thenReturn(Observable.just(false))

        val testObserver = checkAppLockAuthentication.execute().test()
        testObserver.assertValue(AppResume.NOT_AUTHENTICAED)

        verify(checkAuthLazy.get()).execute()

        testObserver.dispose()
    }

    @Test
    fun `should return resume type NEW_APP_LOCK_RESUME for new user when app killed`() {
        whenever(checkAuthLazy.get().execute())
            .thenReturn(Observable.just(true))

        whenever(onboardingPreferencesLazy.get().isNewUser())
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().hasSkippedNameScreen())
            .thenReturn(true)

        whenever(appLockManagerLazy.get().isAppLockActive())
            .thenReturn(false)

        whenever(appLockManagerLazy.get().isAppLockAuthReqd())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().isAppLockEnabled())
            .thenReturn(true)

        whenever(appVariableLazy.get().appCreated)
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().wasAppInBackgroundFor20Minutes())
            .thenReturn(false)

        val testObserver = checkAppLockAuthentication.execute().test()
        testObserver.assertValue(AppResume.NEW_APP_LOCK_RESUME)

        verify(checkAuthLazy.get()).execute()

        testObserver.dispose()
    }

    @Test
    fun `should return resume type NEW_APP_LOCK_RESUME for new user when app in background for 20 minutes`() {
        whenever(checkAuthLazy.get().execute())
            .thenReturn(Observable.just(true))

        whenever(onboardingPreferencesLazy.get().isNewUser())
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().hasSkippedNameScreen())
            .thenReturn(true)

        whenever(appLockManagerLazy.get().isAppLockActive())
            .thenReturn(false)

        whenever(appLockManagerLazy.get().isAppLockAuthReqd())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().isAppLockEnabled())
            .thenReturn(true)

        whenever(appVariableLazy.get().appCreated)
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().wasAppInBackgroundFor20Minutes())
            .thenReturn(true)

        val testObserver = checkAppLockAuthentication.execute().test()
        testObserver.assertValue(AppResume.NEW_APP_LOCK_RESUME)

        verify(checkAuthLazy.get()).execute()

        testObserver.dispose()
    }

    @Test
    fun `should return resume type OLD_APP_LOCK_RESUME for new user when app killed`() {
        whenever(checkAuthLazy.get().execute())
            .thenReturn(Observable.just(true))

        whenever(onboardingPreferencesLazy.get().isNewUser())
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().hasSkippedNameScreen())
            .thenReturn(true)

        whenever(appLockManagerLazy.get().isAppLockActive())
            .thenReturn(true)

        whenever(appLockManagerLazy.get().isAppLockAuthReqd())
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().isAppLockEnabled())
            .thenReturn(false)

        whenever(appVariableLazy.get().appCreated)
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().wasAppInBackgroundFor20Minutes())
            .thenReturn(false)

        val testObserver = checkAppLockAuthentication.execute().test()
        testObserver.assertValue(AppResume.OLD_APP_LOCK_RESUME)

        verify(checkAuthLazy.get()).execute()

        testObserver.dispose()
    }

    @Test
    fun `should return resume type OLD_APP_LOCK_RESUME for new user when app in background for 20 minutes`() {
        whenever(checkAuthLazy.get().execute())
            .thenReturn(Observable.just(true))

        whenever(onboardingPreferencesLazy.get().isNewUser())
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().hasSkippedNameScreen())
            .thenReturn(true)

        whenever(appLockManagerLazy.get().isAppLockActive())
            .thenReturn(true)

        whenever(appLockManagerLazy.get().isAppLockAuthReqd())
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().isAppLockEnabled())
            .thenReturn(false)

        whenever(appVariableLazy.get().appCreated)
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().wasAppInBackgroundFor20Minutes())
            .thenReturn(true)

        val testObserver = checkAppLockAuthentication.execute().test()
        testObserver.assertValue(AppResume.OLD_APP_LOCK_RESUME)

        verify(checkAuthLazy.get()).execute()

        testObserver.dispose()
    }

    @Test
    fun `should return resume type BUSINESS_NAME for new user when business name not entered & skipped`() {
        whenever(checkAuthLazy.get().execute())
            .thenReturn(Observable.just(true))

        whenever(onboardingPreferencesLazy.get().isNewUser())
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().hasSkippedNameScreen())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().hasNameEntered())
            .thenReturn(false)

        whenever(appLockManagerLazy.get().isAppLockActive())
            .thenReturn(false)

        whenever(appLockManagerLazy.get().isAppLockAuthReqd())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().isAppLockEnabled())
            .thenReturn(false)

        whenever(appVariableLazy.get().appCreated)
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().wasAppInBackgroundFor20Minutes())
            .thenReturn(false)

        val testObserver = checkAppLockAuthentication.execute().test()
        testObserver.assertValue(AppResume.BUSINESS_NAME)

        verify(checkAuthLazy.get()).execute()

        testObserver.dispose()
    }

    @Test
    fun `should return resume type APP_LOCK_SETUP for old user when they has app eature & not enabled it`() {
        whenever(checkAuthLazy.get().execute())
            .thenReturn(Observable.just(true))

        whenever(onboardingPreferencesLazy.get().isNewUser())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().hasSkippedNameScreen())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().hasNameEntered())
            .thenReturn(false)

        whenever(appLockManagerLazy.get().isAppLockActive())
            .thenReturn(false)

        whenever(appLockManagerLazy.get().isAppLockAuthReqd())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().existingUserEnabledAppLock())
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().isAppLockEnabled())
            .thenReturn(false)

        whenever(appVariableLazy.get().appCreated)
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().wasAppInBackgroundFor20Minutes())
            .thenReturn(false)

        val testObserver = checkAppLockAuthentication.execute().test()
        testObserver.assertValue(AppResume.APP_LOCK_SETUP)

        verify(checkAuthLazy.get()).execute()

        testObserver.dispose()
    }

    @Test
    fun `should return resume type NONE for new user`() {
        whenever(checkAuthLazy.get().execute())
            .thenReturn(Observable.just(true))

        whenever(onboardingPreferencesLazy.get().isNewUser())
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().hasSkippedNameScreen())
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().hasNameEntered())
            .thenReturn(true)

        whenever(appLockManagerLazy.get().isAppLockActive())
            .thenReturn(false)

        whenever(appLockManagerLazy.get().isAppLockAuthReqd())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().existingUserEnabledAppLock())
            .thenReturn(true)

        whenever(onboardingPreferencesLazy.get().isAppLockEnabled())
            .thenReturn(true)

        whenever(appVariableLazy.get().appCreated)
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().wasAppInBackgroundFor20Minutes())
            .thenReturn(false)

        val testObserver = checkAppLockAuthentication.execute().test()
        testObserver.assertValue(AppResume.NONE)

        verify(checkAuthLazy.get()).execute()

        testObserver.dispose()
    }

    @Test
    fun `should return resume type NONE for old user`() {
        whenever(checkAuthLazy.get().execute())
            .thenReturn(Observable.just(true))

        whenever(onboardingPreferencesLazy.get().isNewUser())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().hasSkippedNameScreen())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().hasNameEntered())
            .thenReturn(false)

        whenever(appLockManagerLazy.get().isAppLockActive())
            .thenReturn(false)

        whenever(appLockManagerLazy.get().isAppLockAuthReqd())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().existingUserEnabledAppLock())
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().isAppLockEnabled())
            .thenReturn(false)

        whenever(appVariableLazy.get().appCreated)
            .thenReturn(false)

        whenever(onboardingPreferencesLazy.get().wasAppInBackgroundFor20Minutes())
            .thenReturn(false)

        val testObserver = checkAppLockAuthentication.execute().test()
        testObserver.assertValue(AppResume.NONE)

        verify(checkAuthLazy.get()).execute()

        testObserver.dispose()
    }
}
