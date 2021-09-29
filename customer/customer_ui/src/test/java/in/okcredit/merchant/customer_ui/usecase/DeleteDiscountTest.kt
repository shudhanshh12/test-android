package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
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

class DeleteDiscountTest {
    private val syncTransactionsImpl: SyncTransactionsImpl = mock()
    private val server: BackendRemoteSource = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private lateinit var discount: DeleteDiscount

    @Before
    fun setup() {
        discount = DeleteDiscount(syncTransactionsImpl, server, { getActiveBusinessId })
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun ` delete discount successfully`() {
        val businessId = "business-id"
        whenever(
            server.deleteDiscount(
                "id",
                businessId
            )
        ).thenReturn(Completable.complete())

        whenever(
            syncTransactionsImpl.execute(
                "delete_discount", null, false, businessId
            )
        ).thenReturn(Completable.complete())

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val testObserver = discount.execute("id").test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(Unit)
        )
    }

    @Test
    fun ` delete discount return error`() {
        val businessId = "business-id"

        val mockError: Exception = mock()
        whenever(
            server.deleteDiscount(
                "id",
                businessId
            )
        ).thenReturn(Completable.error(mockError))

        whenever(
            syncTransactionsImpl.execute(
                "delete_discount", null, false, businessId
            )
        ).thenReturn(Completable.complete())

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val testObserver = discount.execute("id").test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Failure(mockError)
        )
    }

    @Test
    fun ` syncTransactions return error`() {
        val businessId = "business-id"

        val mockError: Exception = mock()
        whenever(
            server.deleteDiscount(
                "id",
                businessId
            )
        ).thenReturn(Completable.complete())

        whenever(
            syncTransactionsImpl.execute(
                "delete_discount", null, false, businessId
            )
        ).thenReturn(Completable.error(mockError))

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val testObserver = discount.execute("id").test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Failure(mockError)
        )
    }
}
