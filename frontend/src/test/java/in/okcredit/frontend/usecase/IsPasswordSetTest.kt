package `in`.okcredit.frontend.usecase

import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.utils.ThreadUtils

class IsPasswordSetTest {
    private val authService: AuthService = mock()
    private lateinit var isPasswordSet: IsPasswordSet

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        isPasswordSet = IsPasswordSet(authService)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
    }

    @Test
    fun `testExecute return true`() {
        // given
        whenever(authService.isPasswordSet()).thenReturn(true)

        // when
        val result = isPasswordSet.execute(Unit).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success(true)
        )
    }

    @Test
    fun `testExecute return false`() {
        // given
        whenever(authService.isPasswordSet()).thenReturn(false)

        // when
        val result = isPasswordSet.execute(Unit).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success(false)
        )
    }
}
