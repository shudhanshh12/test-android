package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.usecases.VerifyPassword

class AddDiscountTest {

    private val verifyPassword: VerifyPassword = mock()
    private val remoteSource: BackendRemoteSource = mock()
    private val syncTransactionsImpl: SyncTransactionsImpl = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var discount: AddDiscount

    @Before
    fun setup() {

        discount = AddDiscount(verifyPassword, remoteSource, syncTransactionsImpl, { getActiveBusinessId })
    }

    @Test
    fun ` complete discount addition when values are correct`() {
        val req = AddDiscount.Request("1", 1L, null, null, false)
        val businessId = "business-id"
        whenever(
            remoteSource.createDiscount(
                any(),
                any(),
                any(),
                anyOrNull(),
                eq(businessId)
            )
        ).thenReturn(Completable.complete())

        whenever(
            syncTransactionsImpl.execute(
                "add_discount", null, false, businessId
            )
        ).thenReturn(Completable.complete())
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val testObserver = discount.execute(req).test()
        testObserver.assertComplete()
    }
}
