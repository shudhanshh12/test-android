package `in`.okcredit.backend._offline.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.usecases.ResetPassword
import tech.okcredit.android.base.rxjava.SchedulerProvider

class ResetPasswordTest {

    private val authService: AuthService = mock()
    private val schedulerProvider: SchedulerProvider = mock()
    private lateinit var resetPassword: ResetPassword

    @Before
    fun setup() {
        resetPassword = ResetPassword(Lazy { authService }, Lazy { schedulerProvider })
    }

    @Test
    fun `should call setPassword() and return a completable`() {
        whenever(schedulerProvider.io()).thenReturn(Schedulers.trampoline())

        val testObserver = resetPassword.execute("000000").test()

        verify(authService, times(1)).setPassword("000000")
        testObserver.assertComplete()
    }
}
