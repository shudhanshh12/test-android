package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class MarkCustomerAsSeenTest {

    private val customerRepo: CustomerRepo = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val markCustomerAsSeen = MarkCustomerAsSeen({ customerRepo }, { getActiveBusinessId })

    private val businessId = "businessId"

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun `MarkCustomerAsSeen() return completable`() {
        whenever(customerRepo.markActivityAsSeen("abc", businessId)).thenReturn(Completable.complete())

        val testObserver = markCustomerAsSeen.execute("abc").test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(Unit)
        )
        testObserver.dispose()
    }

    @Test
    fun `MarkCustomerAsSeen() return error`() {
        val mockError: Exception = mock()
        whenever(customerRepo.markActivityAsSeen("abc", businessId)).thenReturn(Completable.error(mockError))

        val testObserver = markCustomerAsSeen.execute("abc").test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Failure(mockError)
        )
        testObserver.dispose()
    }
}
