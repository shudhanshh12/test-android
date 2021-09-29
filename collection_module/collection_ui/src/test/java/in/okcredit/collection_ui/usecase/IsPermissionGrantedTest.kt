package `in`.okcredit.collection_ui.usecase

import android.content.Context
import android.content.pm.PackageManager
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class IsPermissionGrantedTest {
    private val context: Context = mock()
    private val isPermissionGranted = IsPermissionGranted(context)

    @Test
    fun `is permission granted true`() {
        val res = context.checkCallingOrSelfPermission("android.permission.READ_SMS")
        whenever(res)
            .thenReturn(1)

        val testObserver = isPermissionGranted.execute("android.permission.READ_SMS").test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(1 == PackageManager.PERMISSION_GRANTED)
        )

        verify(context).checkCallingOrSelfPermission("android.permission.READ_SMS")
    }

    @Test
    fun `is permission granted false`() {
        val res = context.checkCallingOrSelfPermission("android.permission.READ_SMS")
        whenever(res)
            .thenReturn(0)

        val testObserver = isPermissionGranted.execute("android.permission.READ_SMS").test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(0 == PackageManager.PERMISSION_GRANTED)
        )

        verify(context).checkCallingOrSelfPermission("android.permission.READ_SMS")
    }
}
