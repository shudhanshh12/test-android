package `in`.okcredit.backend._offline.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.usecases.VerifyPassword

class VerifyPasswordTest {

    private val authService: AuthService = mock()
    private lateinit var verifyPassword: VerifyPassword

    @Before
    fun setup() {
        verifyPassword = VerifyPassword { authService }
    }

    @Test
    fun `should call verify password and return a completable`() {
        val testObserver = verifyPassword.execute("000000").test()

        verify(authService, times(1)).verifyPassword("000000")
        testObserver.assertComplete()
    }
}
