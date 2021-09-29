package `in`.okcredit.backend._offline.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.usecases.IsPasswordSet

class IsPasswordSetTest {

    private val authService: AuthService = mock()
    private lateinit var isPasswordSet: IsPasswordSet

    @Before
    fun setup() {
        isPasswordSet = IsPasswordSet { authService }
    }

    @Test
    fun `should return value returned by isPassword set method of authservice`() {
        whenever(authService.isPasswordSet()).thenReturn(true)

        val testObserver = isPasswordSet.execute().test()

        verify(authService, times(1)).isPasswordSet()
        testObserver.assertValue(true)
    }
}
