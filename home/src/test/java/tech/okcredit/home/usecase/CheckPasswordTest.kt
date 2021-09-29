package tech.okcredit.home.usecase

import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.android.auth.usecases.IsPasswordSet

class CheckPasswordTest {

    private val isPasswordSet: IsPasswordSet = mock()

    private val checkPassword = CheckPassword(Lazy { isPasswordSet })

    @Test
    fun `isPasswordSet returns false`() {
        whenever(isPasswordSet.execute()).thenReturn(
            Single.just(false)
        )

        val testObserver = checkPassword.execute().test()
        testObserver.assertValues(Result.Progress(), Result.Success(false))
    }

    @Test
    fun `isPasswordSet returns true`() {
        whenever(isPasswordSet.execute()).thenReturn(
            Single.just(true)
        )

        val testObserver = checkPassword.execute().test()
        testObserver.assertValues(Result.Progress(), Result.Success(true))
    }
}
