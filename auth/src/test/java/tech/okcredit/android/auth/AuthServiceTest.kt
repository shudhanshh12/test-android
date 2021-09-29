package tech.okcredit.android.auth

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import okhttp3.Interceptor
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import tech.okcredit.android.auth.TestData.MOBILE
import tech.okcredit.android.auth.TestData.PASSWORD
import tech.okcredit.android.auth.TestData.PASSWORD_HASH
import tech.okcredit.android.auth.TestData.VALID_CREDENTIAL_GRANT
import tech.okcredit.android.auth.TestData.VALID_GRANT
import java.util.concurrent.TimeUnit

class AuthServiceTest {

    // subject
    lateinit var authService: AuthService

    // rules
    @get:Rule
    val offlineRule: OfflineRule = OfflineRule { verifyZeroInteractions(authRemoteSource) }

    // mocks
    val authLocalSource: AuthLocalSource = mock()

    val authRemoteSource: AuthRemoteSource = mock()

    val authInterceptor: Interceptor = mock()

    @Before
    fun setup() {
        authService = AuthServiceImpl(Lazy { authLocalSource }, Lazy { authRemoteSource }, Lazy { authInterceptor })
        RxJavaPlugins.reset()
    }

    @After
    fun tearDown() = RxJavaPlugins.reset()

    @Test
    @AssertOffline
    fun `getMobile() returns mobile if available`() {
        `when`(authLocalSource.getMobile()).thenReturn(MOBILE)

        assertThat(authService.getMobile()).isEqualTo(MOBILE)
    }

    @Test
    @AssertOffline
    fun `getMobile() returns null if mobile is not available`() {
        `when`(authLocalSource.getMobile()).thenReturn(null)

        assertThat(authService.getMobile()).isNull()
    }

    @Test
    @AssertOffline
    fun `isAuthenticated() returns true if auth grant is available`() {
        `when`(authLocalSource.getAccessToken()).thenReturn(VALID_GRANT.accessToken)

        assertThat(authService.isAuthenticated()).isTrue()
    }

    @Test
    fun getPassword() {
        whenever(authLocalSource.getPasswordHash()).thenReturn("hashPassword")

        assertThat(authService.getPassword()).contains("hashPassword")
    }

    @Test
    @AssertOffline
    fun `isAuthenticated() returns false if auth grant is not available`() {
        `when`(authLocalSource.getAccessToken()).thenReturn(null)

        assertThat(authService.isAuthenticated()).isFalse()
    }

    @Test
    fun `authenticate() calls server and then saves auth grant, mobile`() {
        `when`(authRemoteSource.authenticate(Credential.Password(MOBILE, PASSWORD))).thenReturn(VALID_CREDENTIAL_GRANT)

        authService.authenticate(Credential.Password(MOBILE, PASSWORD))

        verify(authRemoteSource).authenticate(Credential.Password(MOBILE, PASSWORD))
        verify(authLocalSource).setGrant(VALID_CREDENTIAL_GRANT)
        verify(authLocalSource).setMobile(MOBILE)
    }

    @Test
    fun `logout() from all devices calls server and then deletes all auth data except mobile`() {
        authService.logout("", "")

        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertThat(authService.isAuthenticated()).isFalse()
    }

    @Test
    fun `verifyPassword() returns successfully on if passsword hash matches saved value`() {
        `when`(authLocalSource.getPasswordHash()).thenReturn(PASSWORD_HASH)

        authService.verifyPassword(PASSWORD)
    }

    @Test(expected = IncorrectPassword::class)
    fun `verifyPassword() throws IncorrectPassword password hash is incorrect`() {
        val incorrectPassword = "1234560"
        `when`(authLocalSource.getPasswordHash()).thenReturn(PASSWORD_HASH)

        authService.verifyPassword(incorrectPassword)
    }
}
