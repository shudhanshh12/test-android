package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class TxnAlertDenyActionTest {

    private lateinit var txnAlertDenyAction: TxnAlertDenyAction

    private val backendRemoteSource: BackendRemoteSource = mock()
    private val customerRepo: CustomerRepo = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()

        txnAlertDenyAction = TxnAlertDenyAction(backendRemoteSource, { customerRepo }, { getActiveBusinessId })
    }

    @Test
    fun `execute calls updateFeatureValueRequest and updates buyer map`() {
        assert(TxnAlertDenyAction.Action.DENY == 2)
        val request =
            TxnAlertDenyAction.Request(accountID = TestData.CUSTOMER.id, action = 2)
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            backendRemoteSource.updateFeatureValueRequest(
                request.accountID,
                request.action,
                businessId
            )
        ).thenReturn(Completable.complete())

        whenever(
            customerRepo.getCustomerTxnAlertMap(businessId)
        ).thenReturn(mutableMapOf(request.accountID to true))

        doNothing().whenever(customerRepo).updateBuyerMap(mutableMapOf(request.accountID to false), businessId)

        val testObserver = txnAlertDenyAction.execute(request).test()

        verify(backendRemoteSource).updateFeatureValueRequest(request.accountID, 2, businessId)
        verify(customerRepo).getCustomerTxnAlertMap(businessId)
        verify(customerRepo).updateBuyerMap(mutableMapOf(request.accountID to false), businessId)

        assert(testObserver.values().last() == Result.Success(Unit))
    }
}
