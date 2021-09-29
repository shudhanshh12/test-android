package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.DueInfoRepo
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import org.junit.Before
import org.junit.Test

class NullifyDueDateTest {
    private val dueInfoRepo: DueInfoRepo = mock()

    private val nullifyDueDate = NullifyDueDate(Lazy { dueInfoRepo })

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `test due dalte cleared`() {
        whenever(dueInfoRepo.clearDueDateForCustomer("abc")).thenReturn(Completable.complete())

        val testObserver = nullifyDueDate.execute("abc").test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(Unit)
        )
        testObserver.dispose()
    }

    @Test
    fun `test due dalte return error`() {
        val mockError: Exception = mock()
        whenever(dueInfoRepo.clearDueDateForCustomer("abc")).thenReturn(Completable.error(mockError))

        val testObserver = nullifyDueDate.execute("abc").test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Failure(mockError)
        )
        testObserver.dispose()
    }
}
