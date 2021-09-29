package tech.okcredit.home.usecase

import `in`.okcredit.shared.usecase.Result
import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class IsPermissionGrantedTest {
    private val context: Context = mock()

    val isPermissionGranted = IsPermissionGranted(context)

    @Test
    fun `isPermissionGranted when checkCallingOrSelfPermission returns 1`() {
        whenever(context.checkCallingOrSelfPermission("Some req")).thenReturn(
            1
        )

        val testObserver = isPermissionGranted.execute("Some req").test()

        testObserver.assertValues(Result.Progress(), Result.Success(false))
    }

    @Test
    fun `isPermissionGranted when checkCallingOrSelfPermission returns 0`() {
        whenever(context.checkCallingOrSelfPermission("Some req")).thenReturn(
            0
        )

        val testObserver = isPermissionGranted.execute("Some req").test()

        testObserver.assertValues(Result.Progress(), Result.Success(true))
    }
}
